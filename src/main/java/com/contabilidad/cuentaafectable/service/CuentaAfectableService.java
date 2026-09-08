package com.contabilidad.cuentaafectable.service;

import com.contabilidad.centro.service.CentroService;
import com.contabilidad.clasificacioncuenta.entity.ClasificacionCuenta;
import com.contabilidad.clasificacioncuenta.repository.ClasificacionCuentaRepository;
import com.contabilidad.cuentaafectable.dto.AfectableEtiqueta;
import com.contabilidad.cuentaafectable.dto.CuentaAfectableRequest;
import com.contabilidad.cuentaafectable.dto.CuentaAfectableResponse;
import com.contabilidad.cuentaafectable.dto.CuentaAfectableUpdateRequest;
import com.contabilidad.cuentaafectable.entity.CuentaAfectable;
import com.contabilidad.cuentaafectable.repository.CuentaAfectableRepository;
import com.contabilidad.cuentacontable.entity.CuentaContable;
import com.contabilidad.cuentacontable.repository.CuentaContableRepository;
import com.contabilidad.cuentacontable.service.CuentaContableService;
import com.bien.moneda.entity.Moneda;
import com.bien.moneda.repository.MonedaRepository;
import com.bien.producto.service.ProductoService;
import com.bien.sucursal.repository.SucursalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class CuentaAfectableService {

    @Inject
    AuditContext audit;

    @Inject
    CuentaAfectableRepository repository;

    @Inject
    CuentaContableService cuentaService;

    @Inject
    MonedaRepository monedaRepository;

    @Inject
    SucursalRepository sucursalRepository;

    @Inject
    ProductoService productoService;

    @Inject
    CentroService centroService;

    @Inject
    CuentaContableRepository cuentaRepository;

    @Inject
    ClasificacionCuentaRepository clasificacionRepository;

    public List<CuentaAfectableResponse> listAll() {
        return repository.listOrdenadas().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CuentaAfectableResponse> listByCuenta(Long cuentaId) {
        cuentaService.findEntityOrThrow(cuentaId);
        return repository.listByCuenta(cuentaId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CuentaAfectableResponse findById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    @Transactional
    public CuentaAfectableResponse create(CuentaAfectableRequest request) {
        CuentaContable cuenta = cuentaService.findEntityOrThrow(request.cuentaId());
        if (!"MAY".equals(cuenta.getTipoCuenta())) {
            throw new WebApplicationException(
                    "La cuenta " + cuenta.getCodigo() + " es agrupadora: solo las de mayor son imputables",
                    Response.Status.CONFLICT);
        }
        if (!"V".equals(cuenta.getEstado())) {
            throw new WebApplicationException("La cuenta " + cuenta.getCodigo() + " no está vigente",
                    Response.Status.CONFLICT);
        }
        if (repository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("El código de cuenta afectable ya existe: " + request.codigo(),
                    Response.Status.CONFLICT);
        }
        validarDimensiones(request);

        CuentaAfectable a = new CuentaAfectable();
        a.setCuentaId(cuenta.getId());
        a.setCodigo(request.codigo());
        a.setMonedaId(request.monedaId());
        a.setSucursalId(request.sucursalId());
        a.setCentroId(request.centroId());
        a.setProductoId(request.productoId());
        a.setDescripcion(request.descripcion());
        a.setEstado("V");
        a.setFechaInicio(LocalDate.now());
        a.setUsuario(audit.actor());
        a.setEstacion("SISTEMA");

        repository.persist(a);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(a);
        return toResponse(a);
    }

    @Transactional
    public CuentaAfectableResponse update(Long id, CuentaAfectableUpdateRequest request) {
        CuentaAfectable a = findEntityOrThrow(id);
        if (repository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new WebApplicationException("El código de cuenta afectable ya existe: " + request.codigo(),
                    Response.Status.CONFLICT);
        }

        a.setCodigo(request.codigo());
        a.setDescripcion(request.descripcion());
        a.setUsuario(audit.actor());

        if (!a.getEstado().equals(request.estado())) {
            a.setEstado(request.estado());
            a.setFechaFin("A".equals(request.estado()) ? LocalDate.now() : null);
        }

        return toResponse(a);
    }

    public CuentaAfectable findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Cuenta afectable no encontrada: " + id));
    }

    /**
     * La afectable debe existir y estar vigente para recibir un asiento nuevo.
     * Las dadas de baja conservan sus movimientos históricos y sus saldos.
     */
    public CuentaAfectable findVigenteOrThrow(Long id) {
        CuentaAfectable a = findEntityOrThrow(id);
        if (!"V".equals(a.getEstado())) {
            throw new WebApplicationException(
                    "La cuenta afectable " + a.getCodigo() + " no está vigente: no admite movimientos nuevos.",
                    Response.Status.CONFLICT);
        }
        return a;
    }

    /**
     * Etiquetas de varias afectables en tres consultas fijas (afectables,
     * cuentas, clasificaciones), en vez de una por renglón del asiento.
     */
    public Map<Long, AfectableEtiqueta> etiquetas(Collection<Long> afectableIds) {
        List<CuentaAfectable> afectables = repository.listByIds(afectableIds);
        Map<Long, CuentaContable> cuentas = cuentaRepository
                .listByIds(afectables.stream().map(CuentaAfectable::getCuentaId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(CuentaContable::getId, c -> c));
        Map<Long, String> naturalezas = clasificacionRepository.listAll().stream()
                .collect(Collectors.toMap(ClasificacionCuenta::getId, ClasificacionCuenta::getNaturaleza));

        Map<Long, AfectableEtiqueta> salida = new HashMap<>();
        for (CuentaAfectable a : afectables) {
            CuentaContable cuenta = cuentas.get(a.getCuentaId());
            salida.put(a.getId(), new AfectableEtiqueta(
                    a.getId(),
                    a.getCodigo(),
                    cuenta == null ? null : cuenta.getCodigo(),
                    cuenta == null ? null : cuenta.getNombre(),
                    cuenta == null ? null : naturalezas.get(cuenta.getClasificacionId())));
        }
        return salida;
    }

    // ---- internos ----

    private void validarDimensiones(CuentaAfectableRequest request) {
        if (request.monedaId() != null && monedaRepository.findByIdOptional(request.monedaId()).isEmpty()) {
            throw new NotFoundException("Moneda no encontrada: " + request.monedaId());
        }
        if (request.sucursalId() != null && sucursalRepository.findByIdOptional(request.sucursalId()).isEmpty()) {
            throw new NotFoundException("Sucursal no encontrada: " + request.sucursalId());
        }
        if (request.centroId() != null) {
            centroService.findEntityOrThrow(request.centroId());
        }
        if (request.productoId() != null) {
            productoService.findEntityOrThrow(request.productoId());
        }
    }

    private CuentaAfectableResponse toResponse(CuentaAfectable a) {
        CuentaContable cuenta = cuentaService.findEntityOrThrow(a.getCuentaId());
        String monedaCodigo = a.getMonedaId() == null
                ? null
                : monedaRepository.findByIdOptional(a.getMonedaId()).map(Moneda::getCodigo).orElse(null);
        return CuentaAfectableResponse.from(a, cuenta.getCodigo(), cuenta.getNombre(), monedaCodigo);
    }
}
