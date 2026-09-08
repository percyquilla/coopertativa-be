package com.contabilidad.periodocontable.service;

import com.contabilidad.diacontable.entity.DiaContable;
import com.contabilidad.diacontable.repository.DiaContableRepository;
import com.contabilidad.periodocontable.dto.PeriodoContableResponse;
import com.contabilidad.periodocontable.entity.PeriodoContable;
import com.contabilidad.periodocontable.repository.PeriodoContableRepository;
import com.contabilidad.saldo.service.SaldoCuentaService;
import com.bien.common.security.AuditContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PeriodoContableService {

    @Inject
    AuditContext audit;

    @Inject
    PeriodoContableRepository repository;

    @Inject
    DiaContableRepository diaRepository;

    @Inject
    SaldoCuentaService saldoService;

    public List<PeriodoContableResponse> listAll() {
        return repository.listOrdenados().stream()
                .map(PeriodoContableResponse::from)
                .toList();
    }

    public PeriodoContableResponse findById(Long id) {
        return PeriodoContableResponse.from(findEntityOrThrow(id));
    }

    /** Genera los 12 meses de una gestión. La numeración de comprobantes es por período. */
    @Transactional
    public List<PeriodoContableResponse> generarGestion(Integer gestion) {
        if (repository.existsGestion(gestion)) {
            throw new WebApplicationException("La gestión " + gestion + " ya tiene períodos",
                    Response.Status.CONFLICT);
        }
        String usuario = audit.actor();

        List<PeriodoContableResponse> creados = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            YearMonth ym = YearMonth.of(gestion, mes);
            PeriodoContable p = new PeriodoContable();
            p.setGestion(gestion);
            p.setMes((short) mes);
            p.setFechaInicio(ym.atDay(1));
            p.setFechaFin(ym.atEndOfMonth());
            p.setEstado("A");
            p.setUsuario(usuario);
            p.setEstacion("SISTEMA");
            repository.persist(p);
            generarDias(p, usuario);
            creados.add(PeriodoContableResponse.from(p));
        }
        repository.getEntityManager().flush();
        return creados;
    }

    /**
     * Pre-genera una fila de {@code dia_contable} por cada fecha del período, igual
     * que se generan los 12 meses de la gestión de una vez. Los días pasados quedan
     * en 'C' (frontera limpia) y los futuros en 'P' pendientes de apertura.
     */
    private void generarDias(PeriodoContable p, String usuario) {
        LocalDate hoy = LocalDate.now();
        for (LocalDate d = p.getFechaInicio(); !d.isAfter(p.getFechaFin()); d = d.plusDays(1)) {
            DiaContable dia = new DiaContable();
            dia.setFecha(d);
            dia.setPeriodoId(p.getId());
            dia.setEstado(d.isBefore(hoy) ? "C" : "P");
            dia.setUsuario(usuario);
            dia.setEstacion("SISTEMA");
            diaRepository.persist(dia);
        }
    }

    /**
     * Cierra el período. Exige que no quede ninguno anterior de la misma gestión
     * sin cerrar: los períodos se cierran en orden, si no los saldos de arrastre
     * quedarían mal.
     */
    @Transactional
    public PeriodoContableResponse cerrar(Long id) {
        PeriodoContable p = findEntityOrThrow(id);
        if ("C".equals(p.getEstado())) {
            throw new WebApplicationException("El período ya está cerrado", Response.Status.CONFLICT);
        }
        if (repository.existsAnteriorAbierto(p.getGestion(), p.getMes())) {
            throw new WebApplicationException(
                    "Hay períodos anteriores de la gestión " + p.getGestion() + " sin cerrar",
                    Response.Status.CONFLICT);
        }
        p.setEstado("C");
        p.setUsuario(audit.actor());
        repository.getEntityManager().flush();

        // el saldo con que cierra cada cuenta pasa a ser el saldo inicial del mes siguiente
        saldoService.arrastrarAlSiguiente(p);
        return PeriodoContableResponse.from(p);
    }

    /** Reapertura: queda en 'R' para distinguirla de un período que nunca se cerró. */
    @Transactional
    public PeriodoContableResponse reabrir(Long id) {
        PeriodoContable p = findEntityOrThrow(id);
        if (!"C".equals(p.getEstado())) {
            throw new WebApplicationException("El período no está cerrado, no se puede reabrir.",
                    Response.Status.CONFLICT);
        }
        p.setEstado("R");
        p.setUsuario(audit.actor());
        return PeriodoContableResponse.from(p);
    }

    /** Período abierto que contiene la fecha, para asentar. 409 si está cerrado. */
    public PeriodoContable findAbiertoPorFechaOrThrow(LocalDate fecha) {
        PeriodoContable p = repository.findByFecha(fecha)
                .orElseThrow(() -> new NotFoundException("No hay período contable para la fecha " + fecha));
        if ("C".equals(p.getEstado())) {
            throw new WebApplicationException(
                    "El período " + p.getGestion() + "-" + p.getMes() + " está cerrado",
                    Response.Status.CONFLICT);
        }
        return p;
    }

    public PeriodoContable findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Período contable no encontrado: " + id));
    }
}
