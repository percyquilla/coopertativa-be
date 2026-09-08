package com.bien.solicitudretiro.service;

import com.bien.certificadoaportacion.service.CertificadoAportacionService;
import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.cuentacaptacion.service.CuentaCaptacionService;
import com.bien.movimiento.service.MovimientoService;
import com.bien.socio.entity.Socio;
import com.bien.socio.repository.SocioRepository;
import com.bien.solicitudretiro.dto.ResolucionRequest;
import com.bien.solicitudretiro.dto.SolicitudRetiroRequest;
import com.bien.solicitudretiro.dto.SolicitudRetiroResponse;
import com.bien.solicitudretiro.entity.SolicitudRetiro;
import com.bien.solicitudretiro.repository.SolicitudRetiroRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.contabilidad.motor.ContextoContable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Flujo de retiro del socio: PND → APR | RCH | ANL, y APR → PGD | ANL.
 * RCH, PGD y ANL son terminales.
 */
@ApplicationScoped
public class SolicitudRetiroService {

    @Inject
    AuditContext audit;

    @Inject
    SolicitudRetiroRepository repository;

    @Inject
    SocioRepository socioRepository;

    @Inject
    CertificadoAportacionService certificadoService;

    @Inject
    CuentaCaptacionRepository cuentaRepository;

    @Inject
    CuentaCaptacionService cuentaService;

    @Inject
    MovimientoService movimientoService;

    @Inject
    com.contabilidad.motor.ContabilizadorService contabilizador;

    public List<SolicitudRetiroResponse> listAll() {
        return repository.list("order by fechaSolicitud desc, id desc").stream()
                .map(SolicitudRetiroResponse::from)
                .toList();
    }

    public SolicitudRetiroResponse findById(Long id) {
        return SolicitudRetiroResponse.from(findEntityOrThrow(id));
    }

    public List<SolicitudRetiroResponse> listBySocio(Long socioId) {
        requireSocio(socioId);
        return repository.listBySocio(socioId).stream()
                .map(SolicitudRetiroResponse::from)
                .toList();
    }

    @Transactional
    public SolicitudRetiroResponse create(SolicitudRetiroRequest request) {
        Socio socio = requireSocio(request.socioId());
        if (!"V".equals(socio.getEstado())) {
            throw new WebApplicationException(
                    "El socio no está vigente" + ("R".equals(socio.getEstado()) ? " (está retirado)."
                            : "S".equals(socio.getEstado()) ? " (está suspendido)." : "."),
                    Response.Status.CONFLICT);
        }
        if (repository.existsAbiertaBySocio(socio.getId())) {
            throw new WebApplicationException("El socio ya tiene una solicitud de retiro abierta",
                    Response.Status.CONFLICT);
        }

        SolicitudRetiro s = new SolicitudRetiro();
        s.setSocioId(socio.getId());
        s.setFechaSolicitud(LocalDate.now());
        s.setMotivo(request.motivo());
        s.setEstado("PND");
        s.setUsuario(audit.actor());
        s.setEstacion("SISTEMA");

        repository.persist(s);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(s);
        return SolicitudRetiroResponse.from(s);
    }

    /** Aprueba y deja registrado el monto de aportes a devolver (saldo de la cuenta CA). */
    @Transactional
    public SolicitudRetiroResponse aprobar(Long id, ResolucionRequest request) {
        SolicitudRetiro s = requireEstado(id, "PND");
        s.setMontoAportes(cuentaCapital(s.getSocioId()).getSaldoActual());
        s.setEstado("APR");
        sellarResolucion(s, request);
        return SolicitudRetiroResponse.from(s);
    }

    @Transactional
    public SolicitudRetiroResponse rechazar(Long id, ResolucionRequest request) {
        SolicitudRetiro s = requireEstado(id, "PND");
        if (request == null || request.observacion() == null || request.observacion().isBlank()) {
            throw new BadRequestException("El rechazo requiere una observación");
        }
        s.setEstado("RCH");
        sellarResolucion(s, request);
        return SolicitudRetiroResponse.from(s);
    }

    @Transactional
    public SolicitudRetiroResponse anular(Long id, ResolucionRequest request) {
        SolicitudRetiro s = findEntityOrThrow(id);
        if (!"PND".equals(s.getEstado()) && !"APR".equals(s.getEstado())) {
            throw new WebApplicationException(
                    "Solo se puede anular una solicitud pendiente o aprobada; esta solicitud está "
                            + estadoLabel(s.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        s.setEstado("ANL");
        sellarResolucion(s, request);
        return SolicitudRetiroResponse.from(s);
    }

    /**
     * Paga la solicitud: devuelve el saldo íntegro de la cuenta de aportaciones,
     * cierra esa cuenta y retira al socio. Es la única vía por la que sale dinero
     * de un producto CA — el endpoint público de movimientos rechaza los retiros
     * sobre ese producto.
     * <p>
     * {@code montoAportes} se recalcula aquí: entre la aprobación y el pago el
     * socio pudo seguir aportando, y lo que interesa registrar es lo devuelto.
     */
    @Transactional
    public SolicitudRetiroResponse pagar(Long id, ResolucionRequest request) {
        SolicitudRetiro s = requireEstado(id, "APR");

        CuentaCaptacion cuenta = cuentaCapital(s.getSocioId());
        BigDecimal saldo = cuenta.getSaldoActual();

        // Si la cuenta de aportaciones ya está cerrada (p. ej. la cerró otro
        // proceso), el saldo ya es cero: no hay nada que devolver ni que cerrar,
        // solo se completa el retiro del socio y se marca la solicitud como pagada.
        if (!"C".equals(cuenta.getEstadoCuenta())) {
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                var mov = movimientoService.registrarRetiroInstitucional(
                        cuenta.getId(),
                        saldo,
                        "SOLRET-" + s.getId(),
                        "Devolución de aportaciones por solicitud de retiro " + s.getId(),
                        audit.actor());
                // asiento automático de la devolución de capital
                contabilizador.contabilizar(ContextoContable.de("APO_DEV", LocalDate.now(),
                        "Devolución de aportaciones — solicitud de retiro " + s.getId(),
                        "aportaciones", "movimiento", mov.id(), saldo));
            }
            cuentaService.cerrar(cuenta.getId());
        }

        Socio socio = requireSocio(s.getSocioId());
        socio.setEstado("R");
        socio.setFechaRetiro(LocalDate.now());

        s.setMontoAportes(saldo);
        s.setEstado("PGD");
        s.setFechaDevolucion(LocalDate.now());
        sellarResolucion(s, request);
        return SolicitudRetiroResponse.from(s);
    }

    public SolicitudRetiro findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Solicitud de retiro no encontrada: " + id));
    }

    // ---- internos ----

    private static String estadoLabel(String estado) {
        return switch (estado) {
            case "PND" -> "pendiente";
            case "APR" -> "aprobada";
            case "RCH" -> "rechazada";
            case "PGD" -> "pagada";
            case "ANL" -> "anulada";
            default -> estado;
        };
    }

    private SolicitudRetiro requireEstado(Long id, String estadoEsperado) {
        SolicitudRetiro s = findEntityOrThrow(id);
        if (!estadoEsperado.equals(s.getEstado())) {
            throw new WebApplicationException(
                    "Esta acción solo se puede hacer sobre una solicitud " + estadoLabel(estadoEsperado)
                            + "; esta solicitud está " + estadoLabel(s.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        return s;
    }

    private CuentaCaptacion cuentaCapital(Long socioId) {
        Long cuentaId = certificadoService.findCuentaCapitalOrThrow(socioId);
        return cuentaRepository.findByIdOptional(cuentaId)
                .orElseThrow(() -> new NotFoundException("Cuenta de aportaciones no encontrada: " + cuentaId));
    }

    private Socio requireSocio(Long socioId) {
        return socioRepository.findByIdOptional(socioId)
                .orElseThrow(() -> new NotFoundException("Socio no encontrado: " + socioId));
    }

    private void sellarResolucion(SolicitudRetiro s, ResolucionRequest request) {
        s.setFechaResolucion(LocalDate.now());
        s.setUsuarioResolucion(audit.actor());
        if (request != null && request.observacion() != null && !request.observacion().isBlank()) {
            s.setObservacionResolucion(request.observacion());
        }
    }
}
