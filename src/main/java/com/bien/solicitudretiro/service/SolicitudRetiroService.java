package com.bien.solicitudretiro.service;

import com.bien.certificadoaportacion.entity.CertificadoAportacion;
import com.bien.certificadoaportacion.repository.CertificadoAportacionRepository;
import com.bien.certificadoaportacion.service.CertificadoAportacionService;
import com.bien.common.security.AuditContext;
import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.socio.entity.Socio;
import com.bien.socio.repository.SocioRepository;
import com.bien.solicitudretiro.dto.ResolucionRequest;
import com.bien.solicitudretiro.dto.SolicitudRetiroRequest;
import com.bien.solicitudretiro.dto.SolicitudRetiroResponse;
import com.bien.solicitudretiro.entity.SolicitudRetiro;
import com.bien.solicitudretiro.repository.SolicitudRetiroRepository;
import com.contabilidad.motor.ContabilizadorService;
import com.contabilidad.motor.ContextoContable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retiro de un certificado de aportación (Observación #11).
 * <p>
 * Flujo:
 * <ul>
 *   <li><b>REG</b> (registrada por OFICIAL, eligiendo un certificado VIG del
 *       socio) → aprobar → <b>PND</b>, o eliminar → <b>ELM</b> (eliminación
 *       lógica; no se lista).</li>
 *   <li><b>PND</b> (aprobada) → autorizar (GERENTE) → <b>AUT</b>: se contabiliza
 *       APO_DEV, se guarda el {@code operacion_id} y el certificado pasa a CRR;
 *       o rechazar → <b>RZD</b>.</li>
 *   <li><b>RZD</b> (rechazada) → modificar → vuelve a <b>REG</b>, o eliminar.</li>
 * </ul>
 * Si el certificado es el último vigente del socio, sus cuentas de ahorro
 * deben estar cerradas; al autorizarlo el socio queda retirado. La reversión
 * se habilitará más adelante.
 */
@ApplicationScoped
public class SolicitudRetiroService {

    public static final String REGISTRADA = "REG";
    public static final String APROBADA = "PND";
    public static final String AUTORIZADA = "AUT";
    public static final String RECHAZADA = "RZD";
    /** Eliminación lógica: el registro se conserva pero no se lista. */
    public static final String ELIMINADA = SolicitudRetiroRepository.ELIMINADA;
    /** Estado del flujo anterior; ya no se genera, se conserva para datos existentes. */
    public static final String ANULADA = "ANL";

    private static final Set<String> ELIMINABLES = Set.of(REGISTRADA, RECHAZADA);

    static final String CODIGO_OPERACION_DEVOLUCION = "APO_DEV";

    @Inject
    AuditContext audit;

    @Inject
    SolicitudRetiroRepository repository;

    @Inject
    SocioRepository socioRepository;

    @Inject
    CertificadoAportacionService certificadoService;

    @Inject
    CertificadoAportacionRepository certificadoRepository;

    @Inject
    CuentaCaptacionRepository cuentaRepository;

    @Inject
    ContabilizadorService contabilizador;

    /** Todas las solicitudes, salvo las eliminadas (ELM). */
    public List<SolicitudRetiroResponse> listAll() {
        return repository.listNoEliminadas().stream()
                .map(this::toResponse)
                .toList();
    }

    public SolicitudRetiroResponse findById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    public List<SolicitudRetiroResponse> listBySocio(Long socioId) {
        requireSocio(socioId);
        return repository.listBySocio(socioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SolicitudRetiroResponse create(SolicitudRetiroRequest request) {
        SolicitudRetiro s = new SolicitudRetiro();
        aplicarDatos(s, request, null);
        s.setFechaSolicitud(LocalDate.now());
        s.setEstado(REGISTRADA);
        s.setUsuario(audit.actor());
        s.setEstacion(audit.estacion());

        repository.persist(s);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(s);
        return toResponse(s);
    }

    /**
     * Modifica una solicitud rechazada. Se validan de nuevo socio y certificado
     * y siempre vuelve a REG (debe aprobarse de nuevo). Se limpia la resolución
     * anterior.
     */
    @Transactional
    public SolicitudRetiroResponse update(Long id, SolicitudRetiroRequest request) {
        SolicitudRetiro s = requireEstado(id, RECHAZADA);
        aplicarDatos(s, request, s.getId());
        s.setEstado(REGISTRADA);
        s.setFechaResolucion(null);
        s.setUsuarioResolucion(null);
        s.setObservacionResolucion(null);
        sellar(s);
        return toResponse(s);
    }

    /**
     * Eliminación lógica de una solicitud registrada o rechazada (todavía sin
     * efecto contable): pasa a ELM y deja de listarse.
     */
    @Transactional
    public SolicitudRetiroResponse eliminar(Long id) {
        SolicitudRetiro s = findEntityOrThrow(id);
        if (!ELIMINABLES.contains(s.getEstado())) {
            throw new WebApplicationException(
                    "Solo se puede eliminar una solicitud registrada o rechazada; esta solicitud está "
                            + estadoLabel(s.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        s.setEstado(ELIMINADA);
        sellar(s);
        return toResponse(s);
    }

    /** REG → PND. */
    @Transactional
    public SolicitudRetiroResponse aprobar(Long id, ResolucionRequest request) {
        SolicitudRetiro s = requireEstado(id, REGISTRADA);
        s.setEstado(APROBADA);
        sellar(s);
        sellarResolucion(s, request);
        return toResponse(s);
    }

    /** PND → RZD. Requiere observación. Queda para modificar o eliminar. */
    @Transactional
    public SolicitudRetiroResponse rechazar(Long id, ResolucionRequest request) {
        SolicitudRetiro s = requireEstado(id, APROBADA);
        if (request == null || request.observacion() == null || request.observacion().isBlank()) {
            throw new BadRequestException("El rechazo requiere una observación");
        }
        s.setEstado(RECHAZADA);
        sellar(s);
        sellarResolucion(s, request);
        return toResponse(s);
    }

    /**
     * PND → AUT. Contabiliza la devolución (APO_DEV) por el valor nominal del
     * certificado, guarda el {@code operacion_id} y cierra el certificado (CRR).
     * Si era el último certificado vigente, el socio queda retirado.
     */
    @Transactional
    public SolicitudRetiroResponse autorizar(Long id, ResolucionRequest request) {
        SolicitudRetiro s = requireEstado(id, APROBADA);
        Socio socio = requireSocio(s.getSocioId());
        if (s.getCertificadoId() == null) {
            throw new WebApplicationException(
                    "La solicitud no tiene un certificado asociado; rechácela y modifíquela.",
                    Response.Status.CONFLICT);
        }
        CertificadoAportacion certificado = certificadoService.findEntityOrThrow(s.getCertificadoId());
        // Se vuelve a validar: entre el registro y la autorización pudieron
        // retirarse otros certificados o abrirse cuentas de ahorro.
        boolean esUltimo = exigirCuentasCerradasSiEsUltimo(socio);

        BigDecimal monto = nominal(certificado);
        Long operacionId = contabilizador.contabilizar(ContextoContable.de(CODIGO_OPERACION_DEVOLUCION,
                LocalDate.now(),
                "Retiro de certificado de aportación " + certificado.getNumero()
                        + " — socio " + socio.getNumeroSocio() + " — solicitud " + s.getId(),
                "aportaciones", "solicitud_retiro", s.getId(), monto));
        if (operacionId == null) {
            throw new WebApplicationException(
                    "No se pudo registrar la operación contable del retiro; intente nuevamente.",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        certificadoService.retirar(certificado);
        if (esUltimo) {
            socio.setEstado("R");
            socio.setFechaRetiro(LocalDate.now());
        }

        s.setOperacionId(operacionId);
        s.setMontoAportes(monto);
        s.setEstado(AUTORIZADA);
        s.setFechaDevolucion(LocalDate.now());
        sellar(s);
        sellarResolucion(s, request);
        return toResponse(s);
    }

    public SolicitudRetiro findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Solicitud de retiro no encontrada: " + id));
    }

    // ---- internos ----

    /**
     * Valida socio y certificado y los asigna a la solicitud.
     *
     * @param solicitudId id de la propia solicitud al modificar (se excluye del
     *                    control de solicitudes en curso), o {@code null} al crear
     */
    private void aplicarDatos(SolicitudRetiro s, SolicitudRetiroRequest request, Long solicitudId) {
        Socio socio = requireSocio(request.socioId());
        if (!"V".equals(socio.getEstado())) {
            throw new WebApplicationException(
                    "El socio no está vigente" + ("R".equals(socio.getEstado()) ? " (está retirado)."
                            : "S".equals(socio.getEstado()) ? " (está suspendido)." : "."),
                    Response.Status.CONFLICT);
        }

        CertificadoAportacion certificado = certificadoService.findEntityOrThrow(request.certificadoId());
        if (!socio.getId().equals(certificado.getSocioId())) {
            throw new BadRequestException("El certificado " + certificado.getNumero() + " no pertenece al socio.");
        }
        if (!CertificadoAportacionService.VIGENTE.equals(certificado.getEstado())) {
            throw new WebApplicationException("Solo se puede retirar un certificado vigente; el certificado "
                    + certificado.getNumero() + " está "
                    + CertificadoAportacionService.estadoLabel(certificado.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        if (repository.existsAbiertaByCertificado(certificado.getId(), solicitudId)) {
            throw new WebApplicationException("El certificado " + certificado.getNumero()
                    + " ya tiene una solicitud de retiro en curso.", Response.Status.CONFLICT);
        }
        exigirCuentasCerradasSiEsUltimo(socio);

        s.setSocioId(socio.getId());
        s.setCertificadoId(certificado.getId());
        s.setMotivo(request.motivo());
        s.setMontoAportes(nominal(certificado));
    }

    /**
     * Si el socio tiene un único certificado vigente (el que se retira), exige
     * que todas sus cuentas de ahorro estén cerradas.
     *
     * @return {@code true} si es el último certificado vigente del socio
     */
    private boolean exigirCuentasCerradasSiEsUltimo(Socio socio) {
        if (certificadoRepository.countVigentesBySocio(socio.getId()) > 1) {
            return false;
        }
        List<CuentaCaptacion> abiertas = cuentaRepository.listAhorroNoCerradasByPersona(socio.getPersonaId());
        if (!abiertas.isEmpty()) {
            throw new WebApplicationException(
                    "Es el único certificado vigente del socio: antes de retirarlo debe cerrar sus cuentas de ahorro ("
                            + abiertas.stream().map(CuentaCaptacion::getNumeroCuenta).collect(Collectors.joining(", "))
                            + ").",
                    Response.Status.CONFLICT);
        }
        return true;
    }

    private static BigDecimal nominal(CertificadoAportacion c) {
        return c.getValorUnitario().multiply(BigDecimal.valueOf(c.getCantidad()));
    }

    private static String estadoLabel(String estado) {
        return switch (estado) {
            case REGISTRADA -> "registrada";
            case APROBADA -> "aprobada";
            case AUTORIZADA -> "autorizada";
            case RECHAZADA -> "rechazada";
            case ELIMINADA -> "eliminada";
            case ANULADA -> "anulada";
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

    private Socio requireSocio(Long socioId) {
        return socioRepository.findByIdOptional(socioId)
                .orElseThrow(() -> new NotFoundException("Socio no encontrado: " + socioId));
    }

    private void sellar(SolicitudRetiro s) {
        s.setUsuario(audit.actor());
        s.setFechaHora(LocalDateTime.now());
        s.setEstacion(audit.estacion());
    }

    private void sellarResolucion(SolicitudRetiro s, ResolucionRequest request) {
        s.setFechaResolucion(LocalDate.now());
        s.setUsuarioResolucion(audit.actor());
        if (request != null && request.observacion() != null && !request.observacion().isBlank()) {
            s.setObservacionResolucion(request.observacion());
        }
    }

    private SolicitudRetiroResponse toResponse(SolicitudRetiro s) {
        String numero = s.getCertificadoId() == null ? null
                : certificadoRepository.findByIdOptional(s.getCertificadoId())
                        .map(CertificadoAportacion::getNumero)
                        .orElse(null);
        return SolicitudRetiroResponse.from(s, numero);
    }
}
