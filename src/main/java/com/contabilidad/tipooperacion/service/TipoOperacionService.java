package com.contabilidad.tipooperacion.service;

import com.bien.common.security.AuditContext;
import com.contabilidad.esquema.entity.EsquemaContable;
import com.contabilidad.esquema.service.EsquemaContableService;
import com.contabilidad.tipocomprobante.entity.TipoComprobante;
import com.contabilidad.tipocomprobante.service.TipoComprobanteService;
import com.contabilidad.tipooperacion.dto.TipoOperacionRequest;
import com.contabilidad.tipooperacion.dto.TipoOperacionResponse;
import com.contabilidad.tipooperacion.entity.TipoOperacion;
import com.contabilidad.tipooperacion.repository.TipoOperacionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TipoOperacionService {

    @Inject
    AuditContext audit;

    @Inject
    TipoOperacionRepository repository;

    @Inject
    EsquemaContableService esquemaService;

    @Inject
    TipoComprobanteService tipoComprobanteService;

    public List<TipoOperacionResponse> listAll() {
        return repository.listOrdenados().stream().map(this::toResponse).toList();
    }

    public TipoOperacionResponse findById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    @Transactional
    public TipoOperacionResponse create(TipoOperacionRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("Ya existe un tipo de operación con el código "
                    + request.codigo(), Response.Status.CONFLICT);
        }
        esquemaService.findEntityOrThrow(request.esquemaContableId());
        tipoComprobanteService.findEntityOrThrow(request.tipoComprobanteId());

        TipoOperacion t = new TipoOperacion();
        t.setCodigo(request.codigo());
        t.setDescripcion(request.descripcion());
        t.setEsquemaContableId(request.esquemaContableId());
        t.setTipoComprobanteId(request.tipoComprobanteId());
        t.setEstado(request.estado());
        t.setFechaInicio(LocalDate.now());
        t.setUsuario(audit.actor());
        t.setEstacion("SISTEMA");
        repository.persist(t);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(t);
        return toResponse(t);
    }

    @Transactional
    public TipoOperacionResponse update(Long id, TipoOperacionRequest request) {
        TipoOperacion t = findEntityOrThrow(id);
        if (!t.getCodigo().equals(request.codigo()) && repository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("Ya existe un tipo de operación con el código "
                    + request.codigo(), Response.Status.CONFLICT);
        }
        esquemaService.findEntityOrThrow(request.esquemaContableId());
        tipoComprobanteService.findEntityOrThrow(request.tipoComprobanteId());

        t.setCodigo(request.codigo());
        t.setDescripcion(request.descripcion());
        t.setEsquemaContableId(request.esquemaContableId());
        t.setTipoComprobanteId(request.tipoComprobanteId());
        if (!t.getEstado().equals(request.estado())) {
            t.setEstado(request.estado());
            t.setFechaFin("S".equals(request.estado()) ? LocalDate.now() : null);
        }
        t.setUsuario(audit.actor());
        return toResponse(t);
    }

    public TipoOperacion findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Tipo de operación no encontrado: " + id));
    }

    /** El tipo de operación vigente para un código, o un 409 con qué falta configurar. */
    public TipoOperacion findVigentePorCodigoOrThrow(String codigo) {
        TipoOperacion t = repository.findByCodigo(codigo).orElseThrow(() -> new WebApplicationException(
                "No hay un tipo de operación configurado con el código «" + codigo
                        + "». La operación no se puede contabilizar.", Response.Status.CONFLICT));
        if (!t.estaVigente(LocalDate.now())) {
            throw new WebApplicationException("El tipo de operación «" + codigo
                    + "» no está vigente. La operación no se puede contabilizar.",
                    Response.Status.CONFLICT);
        }
        return t;
    }

    // ---- internos ----

    private TipoOperacionResponse toResponse(TipoOperacion t) {
        EsquemaContable esquema = t.getEsquemaContableId() == null ? null
                : esquemaService.findEntityOrThrow(t.getEsquemaContableId());
        TipoComprobante tipo = t.getTipoComprobanteId() == null ? null
                : tipoComprobanteService.findEntityOrThrow(t.getTipoComprobanteId());
        return TipoOperacionResponse.from(t,
                esquema == null ? null : esquema.getCodigo(),
                esquema == null ? null : esquema.getVersion(),
                tipo == null ? null : tipo.getCodigo());
    }
}
