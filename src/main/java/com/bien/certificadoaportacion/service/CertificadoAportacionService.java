package com.bien.certificadoaportacion.service;

import com.bien.certificadoaportacion.dto.CertificadoAportacionRequest;
import com.bien.certificadoaportacion.dto.CertificadoAportacionResponse;
import com.bien.certificadoaportacion.entity.CertificadoAportacion;
import com.bien.certificadoaportacion.repository.CertificadoAportacionRepository;
import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.cuentacaptacion.service.CuentaCaptacionService;
import com.bien.cuentatitular.dto.CuentaTitularRequest;
import com.bien.cuentatitular.service.CuentaTitularService;
import com.bien.producto.entity.Producto;
import com.bien.producto.service.ProductoService;
import com.bien.socio.entity.Socio;
import com.bien.socio.repository.SocioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CertificadoAportacionService {

    @Inject
    AuditContext audit;

    /** Tipo de producto sobre el que se apoyan las aportaciones (§5.1). */
    private static final String TIPO_PRODUCTO_CA = "CA";

    @Inject
    CertificadoAportacionRepository repository;

    @Inject
    SocioRepository socioRepository;

    @Inject
    ProductoService productoService;

    @Inject
    CuentaCaptacionService cuentaService;

    @Inject
    CuentaCaptacionRepository cuentaRepository;

    @Inject
    CuentaTitularService titularService;

    public List<CertificadoAportacionResponse> listAll() {
        return repository.list("order by fechaEmision desc, id desc").stream()
                .map(this::toResponse)
                .toList();
    }

    public CertificadoAportacionResponse findById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    public List<CertificadoAportacionResponse> listBySocio(Long socioId) {
        requireSocio(socioId);
        return repository.listBySocio(socioId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Emite un certificado. Si el socio todavía no tiene cuenta de capital, se le
     * abre aquí mismo: producto vigente de tipo CA, número {@code CA-<numeroSocio>}
     * y su persona como titular principal. Los certificados posteriores del mismo
     * socio reutilizan esa cuenta.
     */
    @Transactional
    public CertificadoAportacionResponse create(CertificadoAportacionRequest request) {
        Socio socio = requireSocio(request.socioId());
        if (!"V".equals(socio.getEstado())) {
            throw new WebApplicationException(
                    "El socio no está vigente" + ("R".equals(socio.getEstado()) ? " (está retirado)."
                            : "S".equals(socio.getEstado()) ? " (está suspendido)." : "."),
                    Response.Status.CONFLICT);
        }
        if (repository.existsByNumero(request.numero())) {
            throw new WebApplicationException("El número de certificado ya existe: " + request.numero(),
                    Response.Status.CONFLICT);
        }

        Long cuentaId = repository.findCuentaCaBySocio(socio.getId())
                .orElseGet(() -> abrirCuentaCapital(socio, audit.actor()));

        CertificadoAportacion c = new CertificadoAportacion();
        c.setSocioId(socio.getId());
        c.setCuentaCaptacionId(cuentaId);
        c.setNumero(request.numero());
        c.setValorUnitario(request.valorUnitario());
        c.setCantidad(request.cantidad());
        c.setFechaEmision(LocalDate.now());
        c.setEstadoVerif("PND");
        c.setEstado("V");
        c.setUsuario(audit.actor());
        c.setEstacion("SISTEMA");

        repository.persist(c);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(c);
        return toResponse(c);
    }

    /**
     * Anula el título. No toca el saldo de la cuenta de capital: la devolución del
     * dinero solo procede por una solicitud de retiro aprobada y pagada.
     */
    @Transactional
    public CertificadoAportacionResponse anular(Long id) {
        CertificadoAportacion c = findEntityOrThrow(id);
        if ("A".equals(c.getEstado())) {
            throw new WebApplicationException("El certificado ya está anulado", Response.Status.CONFLICT);
        }
        c.setEstado("A");
        return toResponse(c);
    }

    /** Cuenta CA del socio, para el flujo de solicitud de retiro. */
    public Long findCuentaCapitalOrThrow(Long socioId) {
        return repository.findCuentaCaBySocio(socioId)
                .orElseThrow(() -> new WebApplicationException(
                        "El socio no tiene cuenta de aportaciones", Response.Status.CONFLICT));
    }

    public CertificadoAportacion findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Certificado de aportación no encontrado: " + id));
    }

    // ---- internos ----

    private Long abrirCuentaCapital(Socio socio, String usuario) {
        Producto producto = productoService.findVigenteByTipoOrThrow(TIPO_PRODUCTO_CA);
        String numeroCuenta = "CA-" + socio.getNumeroSocio();
        if (cuentaService.existsByNumeroCuenta(numeroCuenta)) {
            throw new WebApplicationException("Ya existe una cuenta con el número " + numeroCuenta,
                    Response.Status.CONFLICT);
        }
        CuentaCaptacion cuenta = cuentaService.abrir(producto, numeroCuenta, "IND", usuario);
        titularService.agregar(cuenta.getId(), new CuentaTitularRequest(socio.getPersonaId(), "P", usuario));
        return cuenta.getId();
    }

    private Socio requireSocio(Long socioId) {
        return socioRepository.findByIdOptional(socioId)
                .orElseThrow(() -> new NotFoundException("Socio no encontrado: " + socioId));
    }

    private CertificadoAportacionResponse toResponse(CertificadoAportacion c) {
        if (c.getCuentaCaptacionId() == null) {
            return CertificadoAportacionResponse.from(c, null, null);
        }
        return cuentaRepository.findByIdOptional(c.getCuentaCaptacionId())
                .map(cuenta -> CertificadoAportacionResponse.from(c, cuenta.getNumeroCuenta(), cuenta.getSaldoActual()))
                .orElseGet(() -> CertificadoAportacionResponse.from(c, null, null));
    }
}
