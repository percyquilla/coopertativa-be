package com.contabilidad.diacontable.service;

import com.bien.common.security.AuditContext;
import com.bien.interes.service.InteresService;
import com.contabilidad.comprobante.repository.ComprobanteRepository;
import com.contabilidad.diacontable.dto.DiaContableResponse;
import com.contabilidad.diacontable.entity.DiaContable;
import com.contabilidad.diacontable.repository.DiaContableRepository;
import com.contabilidad.operacion.repository.OperacionContableRepository;
import com.contabilidad.periodocontable.entity.PeriodoContable;
import com.contabilidad.periodocontable.repository.PeriodoContableRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDate;

/**
 * Cierre diario (EOD). El día contable se maneja igual que el período contable:
 * estados 'P' pendiente · 'A' abierto · 'C' cerrado · 'R' reabierto; toda fecha
 * de un período no cerrado tiene su fila (la genera {@code PeriodoContableService});
 * solo un día en 'A'/'R' a la vez; se abren y cierran en orden. Un día no abierto
 * —o sin fila— rechaza asentar, a mano o por el motor.
 */
@ApplicationScoped
public class DiaContableService {

    private static final Logger LOG = Logger.getLogger(DiaContableService.class);

    @Inject
    AuditContext audit;

    @Inject
    InteresService interesService;

    @Inject
    DiaContableRepository repository;

    @Inject
    PeriodoContableRepository periodoRepository;

    @Inject
    OperacionContableRepository operacionRepository;

    @Inject
    ComprobanteRepository comprobanteRepository;

    public DiaContableResponse estado(LocalDate fecha) {
        DiaContable dia = repository.findByFecha(fecha).orElse(null);
        PeriodoContable periodo = periodoRepository.findByFecha(fecha).orElse(null);

        long pendientes = operacionRepository.countByEstadoYDia("PND", fecha);
        long borradores = comprobanteRepository.countByEstadoYDia("REG", fecha);
        String estado = dia == null ? "SIN_ABRIR" : dia.getEstado();
        boolean puedeCerrar = dia != null && dia.estaAbierto() && pendientes == 0 && borradores == 0;

        return new DiaContableResponse(
                fecha, estado,
                periodo == null ? null : periodo.getId(),
                periodo == null ? null : periodo.getGestion(),
                periodo == null ? null : periodo.getMes(),
                periodo != null && "C".equals(periodo.getEstado()),
                pendientes, borradores, puedeCerrar,
                interesService.devengadosEn(fecha));
    }

    /**
     * Abre el día: pasa de 'P' a 'A' (o crea la fila en 'A' si el período no la
     * tenía pre-generada, p. ej. tras reabrir un período). Exige que no haya otro
     * día abierto, que los días anteriores ya estén listos y que no sea anterior a
     * uno ya cerrado.
     */
    @Transactional
    public DiaContableResponse abrir(LocalDate fecha) {
        DiaContable dia = repository.findByFecha(fecha).orElse(null);
        if (dia != null && !dia.estaPendiente()) {
            if (dia.estaCerrado()) {
                throw new WebApplicationException("El día " + fecha
                        + " ya está cerrado. Usá «reabrir» si necesitás corregirlo.",
                        Response.Status.CONFLICT);
            }
            throw new WebApplicationException("El día " + fecha + " ya está abierto.",
                    Response.Status.CONFLICT);
        }

        PeriodoContable periodo = periodoRepository.findByFecha(fecha)
                .orElseThrow(() -> new NotFoundException(
                        "No hay período contable que contenga la fecha " + fecha + "."));
        if ("C".equals(periodo.getEstado())) {
            throw new WebApplicationException("El período " + periodo.getGestion() + "-"
                    + periodo.getMes() + " está cerrado: no se puede abrir un día dentro de él.",
                    Response.Status.CONFLICT);
        }

        requireSinOtroDiaAbierto(fecha, "abrir");
        requireDiasAnterioresListos(fecha);
        requireSinDiaCerradoPosterior(fecha);

        if (dia == null) {
            dia = new DiaContable();
            dia.setFecha(fecha);
            dia.setPeriodoId(periodo.getId());
            dia.setEstacion(audit.estacion());
        }
        dia.setEstado("A");
        dia.setUsuario(audit.actor());
        if (dia.getId() == null) {
            repository.persist(dia);
        }
        repository.getEntityManager().flush();
        return estado(fecha);
    }

    @Transactional
    public DiaContableResponse cerrar(LocalDate fecha) {
        DiaContable dia = requireDia(fecha);
        if (!dia.estaAbierto()) {
            throw new WebApplicationException(dia.estaCerrado()
                    ? "El día " + fecha + " ya está cerrado."
                    : "El día " + fecha + " no está abierto: no hay nada que cerrar.",
                    Response.Status.CONFLICT);
        }

        long pendientes = operacionRepository.countByEstadoYDia("PND", fecha);
        if (pendientes > 0) {
            throw new WebApplicationException("El día " + fecha + " tiene " + pendientes
                    + " operación(es) sin contabilizar. Resolvé la cola de pendientes antes de cerrar.",
                    Response.Status.CONFLICT);
        }
        long borradores = comprobanteRepository.countByEstadoYDia("REG", fecha);
        if (borradores > 0) {
            throw new WebApplicationException("El día " + fecha + " tiene " + borradores
                    + " comprobante(s) en borrador. Autorizalos o anulalos antes de cerrar.",
                    Response.Status.CONFLICT);
        }

        // Fase 6 — intereses: se devengan (y a fin de mes se capitalizan) con el día
        // todavía abierto, para que el asiento se feche en un día que acepta asientos.
        // Nunca frena el cierre: si algo falla, queda registrado y va a la cola PND.
        devengarYCapitalizarIntereses(fecha);

        dia.setEstado("C");
        dia.setUsuario(audit.actor());
        dia.setEstacion(audit.estacion());
        return estado(fecha);
    }

    private void devengarYCapitalizarIntereses(LocalDate fecha) {
        try {
            interesService.devengarDia(fecha);
            if (fecha.equals(fecha.withDayOfMonth(fecha.lengthOfMonth()))) {
                interesService.capitalizarMes(fecha);
            }
        } catch (RuntimeException e) {
            LOG.errorf(e, "Fallo al devengar/capitalizar intereses del %s; el cierre del día continúa.",
                    fecha);
        }
    }

    /** Reapertura: queda en 'R' para distinguirla de un día que nunca se cerró. */
    @Transactional
    public DiaContableResponse reabrir(LocalDate fecha) {
        DiaContable dia = requireDia(fecha);
        if (!dia.estaCerrado()) {
            throw new WebApplicationException("El día " + fecha + " no está cerrado.",
                    Response.Status.CONFLICT);
        }
        PeriodoContable periodo = periodoRepository.findByFecha(fecha).orElse(null);
        if (periodo != null && "C".equals(periodo.getEstado())) {
            throw new WebApplicationException("El período que contiene al " + fecha
                    + " está cerrado; reabrí el período primero.", Response.Status.CONFLICT);
        }
        requireSinOtroDiaAbierto(fecha, "reabrir");
        dia.setEstado("R");
        dia.setUsuario(audit.actor());
        dia.setEstacion(audit.estacion());
        return estado(fecha);
    }

    /**
     * Rechaza asentar en una fecha cuyo día no está abierto. A diferencia del
     * período, acá una fecha sin fila también se rechaza: hay que abrir el día.
     * La usan el alta manual de comprobantes y el motor.
     */
    public void requireDiaAbierto(LocalDate fecha) {
        DiaContable dia = repository.findByFecha(fecha).orElseThrow(() -> new NotFoundException(
                "El día " + fecha + " no está habilitado. Abrí el día contable antes de "
                        + "registrar asientos con esa fecha."));
        if (dia.estaCerrado()) {
            throw new WebApplicationException("El día " + fecha
                    + " está cerrado: no se pueden registrar asientos con esa fecha.",
                    Response.Status.CONFLICT);
        }
        if (dia.estaPendiente()) {
            throw new WebApplicationException("El día " + fecha
                    + " todavía no fue abierto. Abrilo desde «Cierre diario» antes de asentar.",
                    Response.Status.CONFLICT);
        }
    }

    /**
     * Solo puede haber un día contable abierto a la vez: para abrir o reabrir uno
     * hay que cerrar el que esté abierto.
     */
    private void requireSinOtroDiaAbierto(LocalDate fecha, String accion) {
        repository.findOtroAbierto(fecha).ifPresent(abierto -> {
            throw new WebApplicationException("Ya hay un día contable abierto (" + abierto.getFecha()
                    + "). Cerralo antes de " + accion + " otro día.", Response.Status.CONFLICT);
        });
    }

    /** Los días se abren en orden: no se abre uno si queda otro anterior sin abrir. */
    private void requireDiasAnterioresListos(LocalDate fecha) {
        repository.findPendienteAnterior(fecha).ifPresent(pendiente -> {
            throw new WebApplicationException("Antes de abrir el " + fecha + " tenés que abrir y "
                    + "cerrar los días anteriores. El " + pendiente.getFecha() + " sigue sin abrir.",
                    Response.Status.CONFLICT);
        });
    }

    /** No se abre un día anterior a uno ya cerrado (sin back-dating); para eso está «reabrir». */
    private void requireSinDiaCerradoPosterior(LocalDate fecha) {
        repository.findCerradoPosterior(fecha).ifPresent(posterior -> {
            throw new WebApplicationException("No se puede abrir el " + fecha + " porque el "
                    + posterior.getFecha() + " ya está cerrado. Usá «reabrir» para corregir un día "
                    + "ya cerrado.", Response.Status.CONFLICT);
        });
    }

    private DiaContable requireDia(LocalDate fecha) {
        return repository.findByFecha(fecha)
                .orElseThrow(() -> new NotFoundException("El día " + fecha
                        + " no está registrado; abrilo antes de operarlo."));
    }
}
