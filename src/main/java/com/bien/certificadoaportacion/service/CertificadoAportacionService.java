package com.bien.certificadoaportacion.service;

import com.bien.certificadoaportacion.dto.CertificadoAportacionRequest;
import com.bien.certificadoaportacion.dto.CertificadoAportacionResponse;
import com.bien.certificadoaportacion.dto.ValoresEmisionResponse;
import com.bien.certificadoaportacion.entity.CertificadoAportacion;
import com.bien.certificadoaportacion.repository.CertificadoAportacionRepository;
import com.bien.common.security.AuditContext;
import com.bien.parametros.service.ParametroLookup;
import com.bien.socio.entity.Socio;
import com.bien.socio.repository.SocioRepository;
import com.contabilidad.motor.ContabilizadorService;
import com.contabilidad.motor.ContextoContable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Emisión de certificados de aportación (Observaciones #7, #8, #10).
 * <p>
 * Flujo:
 * <ul>
 *   <li><b>REG</b> (registrado por OFICIAL) → aprobar → <b>PND</b>, o eliminar
 *       → <b>ELM</b> (eliminación lógica; no se lista).</li>
 *   <li><b>PND</b> (aprobado) → autorizar (GERENTE) → <b>VIG</b>: se contabiliza
 *       APO_EMI y se guarda el {@code operacion_id}; o rechazar → <b>RZD</b>.</li>
 *   <li><b>RZD</b> (rechazado) → modificar → vuelve a <b>REG</b>, o eliminar.</li>
 *   <li><b>VIG</b> → <b>CRR</b> cuando se autoriza su solicitud de retiro.</li>
 * </ul>
 * La reversión de un certificado autorizado se habilitará más adelante.
 */
@ApplicationScoped
public class CertificadoAportacionService {

    public static final String REGISTRADO = "REG";
    public static final String APROBADO = "PND";
    public static final String VIGENTE = CertificadoAportacionRepository.VIGENTE;
    public static final String RETIRADO = "CRR";
    public static final String RECHAZADO = "RZD";
    /** Eliminación lógica: el registro se conserva pero no se lista. */
    public static final String ELIMINADO = CertificadoAportacionRepository.ELIMINADO;
    /** Estado del flujo anterior; ya no se genera, se conserva para datos existentes. */
    public static final String ANULADO = "ANL";

    private static final Set<String> ELIMINABLES = Set.of(REGISTRADO, RECHAZADO);

    static final String PARAM_VALOR_UNITARIO = "VALOR_UNIT_APORTE";
    static final BigDecimal VALOR_UNITARIO_DEFAULT = new BigDecimal("500.00");
    static final String CODIGO_OPERACION_EMISION = "APO_EMI";

    @Inject
    AuditContext audit;

    @Inject
    CertificadoAportacionRepository repository;

    @Inject
    SocioRepository socioRepository;

    @Inject
    ParametroLookup parametros;

    @Inject
    ContabilizadorService contabilizador;

    /** Todos los certificados, salvo los eliminados (ELM). */
    public List<CertificadoAportacionResponse> listAll() {
        return repository.listNoEliminados().stream()
                .map(CertificadoAportacionResponse::from)
                .toList();
    }

    public CertificadoAportacionResponse findById(Long id) {
        return CertificadoAportacionResponse.from(findEntityOrThrow(id));
    }

    public List<CertificadoAportacionResponse> listBySocio(Long socioId) {
        requireSocio(socioId);
        return repository.listBySocio(socioId).stream()
                .map(CertificadoAportacionResponse::from)
                .toList();
    }

    /** Certificados VIG del socio: los únicos que se pueden retirar. */
    public List<CertificadoAportacionResponse> listVigentesBySocio(Long socioId) {
        requireSocio(socioId);
        return repository.listVigentesBySocio(socioId).stream()
                .map(CertificadoAportacionResponse::from)
                .toList();
    }

    /** Valores por defecto para el formulario de emisión. */
    public ValoresEmisionResponse valoresEmision() {
        return new ValoresEmisionResponse(formatearNumero(repository.maxNumero() + 1), valorUnitarioDefault());
    }

    /** Registra la emisión en estado REG con número correlativo de 6 dígitos. */
    @Transactional
    public CertificadoAportacionResponse create(CertificadoAportacionRequest request) {
        Socio socio = requireSocioVigente(request.socioId());

        repository.bloquearNumeracion();
        String numero = formatearNumero(repository.maxNumero() + 1);

        CertificadoAportacion c = new CertificadoAportacion();
        c.setSocioId(socio.getId());
        c.setNumero(numero);
        c.setValorUnitario(request.valorUnitario() != null ? request.valorUnitario() : valorUnitarioDefault());
        c.setCantidad(request.cantidad());
        c.setFechaEmision(LocalDate.now());
        c.setEstadoVerif("PND");
        c.setEstado(REGISTRADO);
        c.setUsuario(audit.actor());
        c.setEstacion(audit.estacion());

        repository.persist(c);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(c);
        return CertificadoAportacionResponse.from(c);
    }

    /**
     * Modifica una emisión rechazada. Siempre vuelve a REG (debe aprobarse de
     * nuevo). El número de certificado no cambia.
     */
    @Transactional
    public CertificadoAportacionResponse update(Long id, CertificadoAportacionRequest request) {
        CertificadoAportacion c = requireEstado(id, RECHAZADO);
        Socio socio = requireSocioVigente(request.socioId());
        c.setSocioId(socio.getId());
        c.setValorUnitario(request.valorUnitario() != null ? request.valorUnitario() : valorUnitarioDefault());
        c.setCantidad(request.cantidad());
        c.setEstado(REGISTRADO);
        sellar(c);
        return CertificadoAportacionResponse.from(c);
    }

    /**
     * Eliminación lógica de una emisión registrada o rechazada (todavía sin
     * efecto contable): pasa a ELM y deja de listarse. El número no se reutiliza.
     */
    @Transactional
    public CertificadoAportacionResponse eliminar(Long id) {
        CertificadoAportacion c = findEntityOrThrow(id);
        if (!ELIMINABLES.contains(c.getEstado())) {
            throw new WebApplicationException(
                    "Solo se puede eliminar un certificado registrado o rechazado; este está "
                            + estadoLabel(c.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        c.setEstado(ELIMINADO);
        sellar(c);
        return CertificadoAportacionResponse.from(c);
    }

    /** REG → PND. Vuelve a sellar usuario, fecha/hora y estación. */
    @Transactional
    public CertificadoAportacionResponse aprobar(Long id) {
        CertificadoAportacion c = requireEstado(id, REGISTRADO);
        c.setEstado(APROBADO);
        sellar(c);
        return CertificadoAportacionResponse.from(c);
    }

    /** PND → RCH. Queda para modificar o eliminar. */
    @Transactional
    public CertificadoAportacionResponse rechazar(Long id) {
        CertificadoAportacion c = requireEstado(id, APROBADO);
        c.setEstado(RECHAZADO);
        sellar(c);
        return CertificadoAportacionResponse.from(c);
    }

    /**
     * PND → VIG. Genera la operación contable APO_EMI por el valor nominal y
     * guarda su id. Si el asiento no se puede generar en este momento, la
     * operación queda pendiente (reprocesable) y el certificado igual se
     * autoriza, enlazado a esa operación.
     */
    @Transactional
    public CertificadoAportacionResponse autorizar(Long id) {
        CertificadoAportacion c = requireEstado(id, APROBADO);
        BigDecimal nominal = c.getValorUnitario().multiply(BigDecimal.valueOf(c.getCantidad()));
        Socio socio = requireSocio(c.getSocioId());

        Long operacionId = contabilizador.contabilizar(ContextoContable.de(CODIGO_OPERACION_EMISION,
                LocalDate.now(),
                "Emisión de certificado de aportación " + c.getNumero() + " — socio " + socio.getNumeroSocio(),
                "aportaciones", "certificado_aportacion", c.getId(), nominal));
        if (operacionId == null) {
            throw new WebApplicationException(
                    "No se pudo registrar la operación contable de la emisión; intente nuevamente.",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        c.setOperacionId(operacionId);
        c.setEstado(VIGENTE);
        sellar(c);
        return CertificadoAportacionResponse.from(c);
    }

    /** VIG → CRR, invocado al autorizar la solicitud de retiro. */
    public void retirar(CertificadoAportacion c) {
        if (!VIGENTE.equals(c.getEstado())) {
            throw new WebApplicationException(
                    "El certificado " + c.getNumero() + " no está vigente; está " + estadoLabel(c.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        c.setEstado(RETIRADO);
        sellar(c);
    }

    public CertificadoAportacion findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Certificado de aportación no encontrado: " + id));
    }

    public static String estadoLabel(String estado) {
        if (estado == null) {
            return "sin estado";
        }
        return switch (estado) {
            case REGISTRADO -> "registrado";
            case APROBADO -> "aprobado";
            case VIGENTE -> "vigente";
            case RETIRADO -> "retirado";
            case RECHAZADO -> "rechazado";
            case ELIMINADO -> "eliminado";
            case ANULADO -> "anulado";
            default -> estado;
        };
    }

    // ---- internos ----

    private CertificadoAportacion requireEstado(Long id, String estadoEsperado) {
        CertificadoAportacion c = findEntityOrThrow(id);
        if (!estadoEsperado.equals(c.getEstado())) {
            throw new WebApplicationException(
                    "Esta acción solo se puede hacer sobre un certificado " + estadoLabel(estadoEsperado)
                            + "; este certificado está " + estadoLabel(c.getEstado()) + ".",
                    Response.Status.CONFLICT);
        }
        return c;
    }

    private void sellar(CertificadoAportacion c) {
        c.setUsuario(audit.actor());
        c.setFechaHora(LocalDateTime.now());
        c.setEstacion(audit.estacion());
    }

    private BigDecimal valorUnitarioDefault() {
        return parametros.decimalValue(PARAM_VALOR_UNITARIO, VALOR_UNITARIO_DEFAULT);
    }

    private static String formatearNumero(long n) {
        return String.format("%06d", n);
    }

    private Socio requireSocio(Long socioId) {
        return socioRepository.findByIdOptional(socioId)
                .orElseThrow(() -> new NotFoundException("Socio no encontrado: " + socioId));
    }

    private Socio requireSocioVigente(Long socioId) {
        Socio socio = requireSocio(socioId);
        if (!"V".equals(socio.getEstado())) {
            throw new WebApplicationException(
                    "El socio no está vigente" + ("R".equals(socio.getEstado()) ? " (está retirado)."
                            : "S".equals(socio.getEstado()) ? " (está suspendido)." : "."),
                    Response.Status.CONFLICT);
        }
        return socio;
    }
}
