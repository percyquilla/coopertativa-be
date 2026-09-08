package com.contabilidad.cuentacontable.service;

import com.contabilidad.clasificacioncuenta.entity.ClasificacionCuenta;
import com.contabilidad.clasificacioncuenta.service.ClasificacionCuentaService;
import com.contabilidad.cuentacontable.dto.CuentaContableRequest;
import com.contabilidad.cuentacontable.dto.CuentaContableResponse;
import com.contabilidad.cuentacontable.dto.CuentaContableUpdateRequest;
import com.contabilidad.cuentacontable.entity.CuentaContable;
import com.contabilidad.cuentacontable.repository.CuentaContableRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class CuentaContableService {

    @Inject
    AuditContext audit;

    @Inject
    CuentaContableRepository repository;

    @Inject
    ClasificacionCuentaService clasificacionService;

    /**
     * Todo el plan de cuentas ordenado por código. Resuelve padre, clasificación
     * y "tiene hijos" con los datos ya cargados, sin una consulta por fila.
     */
    public List<CuentaContableResponse> listAll() {
        List<CuentaContable> cuentas = repository.listOrdenadas();
        Map<Long, CuentaContable> porId = cuentas.stream()
                .collect(Collectors.toMap(CuentaContable::getId, Function.identity()));
        Set<Long> conHijos = cuentas.stream()
                .map(CuentaContable::getCuentaPadreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        return cuentas.stream()
                .map(c -> toResponse(c, porId.get(c.getCuentaPadreId()), conHijos.contains(c.getId())))
                .toList();
    }

    /** Solo las cuentas de mayor vigentes: las únicas imputables. */
    public List<CuentaContableResponse> listImputables() {
        return repository.listImputables().stream()
                .map(c -> toResponse(c, findPadre(c), false))
                .toList();
    }

    public CuentaContableResponse findById(Long id) {
        CuentaContable c = findEntityOrThrow(id);
        return toResponse(c, findPadre(c), repository.hasHijos(id));
    }

    @Transactional
    public CuentaContableResponse create(CuentaContableRequest request) {
        clasificacionService.findEntityOrThrow(request.clasificacionId());

        if (repository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("El código de cuenta ya existe: " + request.codigo(),
                    Response.Status.CONFLICT);
        }

        short nivel = 1;
        if (request.cuentaPadreId() != null) {
            CuentaContable padre = findEntityOrThrow(request.cuentaPadreId());
            if (!"AGR".equals(padre.getTipoCuenta())) {
                throw new WebApplicationException(
                        "La cuenta padre " + padre.getCodigo() + " es de mayor: no admite cuentas hijas",
                        Response.Status.CONFLICT);
            }
            if (!request.codigo().startsWith(padre.getCodigo())) {
                throw new BadRequestException(
                        "El código debe empezar con el de la cuenta padre (" + padre.getCodigo() + ")");
            }
            nivel = (short) (padre.getNivel() + 1);
        }

        CuentaContable c = new CuentaContable();
        c.setCuentaPadreId(request.cuentaPadreId());
        c.setClasificacionId(request.clasificacionId());
        c.setTipoCuenta(request.tipoCuenta());
        c.setCodigo(request.codigo());
        c.setNombre(request.nombre());
        c.setDescripcion(request.descripcion());
        c.setNivel(nivel);
        c.setEstado("V");
        c.setFechaInicio(LocalDate.now());
        c.setUsuario(audit.actor());
        c.setEstacion("SISTEMA");

        repository.persist(c);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(c);
        return toResponse(c, findPadre(c), false);
    }

    @Transactional
    public CuentaContableResponse update(Long id, CuentaContableUpdateRequest request) {
        CuentaContable c = findEntityOrThrow(id);
        boolean tieneHijos = repository.hasHijos(id);

        if ("A".equals(request.estado()) && tieneHijos) {
            throw new WebApplicationException(
                    "No se puede dar de baja una cuenta con cuentas hijas", Response.Status.CONFLICT);
        }

        c.setNombre(request.nombre());
        c.setDescripcion(request.descripcion());
        c.setUsuario(audit.actor());

        if (!c.getEstado().equals(request.estado())) {
            c.setEstado(request.estado());
            c.setFechaFin("A".equals(request.estado()) ? LocalDate.now() : null);
        }

        return toResponse(c, findPadre(c), tieneHijos);
    }

    public CuentaContable findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Cuenta contable no encontrada: " + id));
    }

    // ---- internos ----

    private CuentaContable findPadre(CuentaContable c) {
        return c.getCuentaPadreId() == null
                ? null
                : repository.findByIdOptional(c.getCuentaPadreId()).orElse(null);
    }

    private CuentaContableResponse toResponse(CuentaContable c, CuentaContable padre, boolean tieneHijos) {
        ClasificacionCuenta clas = clasificacionService.findEntityOrThrow(c.getClasificacionId());
        return CuentaContableResponse.from(
                c,
                padre != null ? padre.getCodigo() : null,
                clas.getCodigo(),
                clas.getNaturaleza(),
                tieneHijos);
    }
}
