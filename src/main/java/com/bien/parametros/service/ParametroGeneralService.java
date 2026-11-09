package com.bien.parametros.service;

import com.bien.parametros.dto.ParametroGeneralRequest;
import com.bien.parametros.dto.ParametroGeneralResponse;
import com.bien.parametros.dto.ParametroGeneralUpdateRequest;
import com.bien.parametros.entity.ParametroGeneral;
import com.bien.parametros.repository.ParametroGeneralRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class ParametroGeneralService {

    @Inject
    AuditContext audit;

    @Inject
    ParametroGeneralRepository repository;

    public List<ParametroGeneralResponse> listAll() {
        return repository.listAll().stream()
                .map(ParametroGeneralResponse::from)
                .toList();
    }

    public ParametroGeneralResponse findById(Long id) {
        return ParametroGeneralResponse.from(findEntityOrThrow(id));
    }

    public ParametroGeneralResponse findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .map(ParametroGeneralResponse::from)
                .orElseThrow(() -> new NotFoundException("Parámetro no encontrado: " + codigo));
    }

    @Transactional
    public ParametroGeneralResponse create(ParametroGeneralRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("El código de parámetro ya existe: " + request.codigo(), Response.Status.CONFLICT);
        }
        ParametroGeneral p = new ParametroGeneral();
        p.setCodigo(request.codigo());
        p.setNombre(request.nombre());
        p.setDescripcion(request.descripcion());
        p.setValor(request.valor());
        p.setTipoDato(request.tipoDato());
        p.setEditable(request.editable() == null || request.editable());
        p.setEstado("V");
        p.setUsuario(audit.actor());
        p.setEstacion(audit.estacion());
        repository.persist(p);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(p);
        return ParametroGeneralResponse.from(p);
    }

    @Transactional
    public ParametroGeneralResponse update(Long id, ParametroGeneralUpdateRequest request) {
        ParametroGeneral p = findEntityOrThrow(id);
        if (!p.isEditable()) {
            throw new WebApplicationException("El parámetro " + p.getCodigo() + " no es editable", Response.Status.CONFLICT);
        }
        p.setNombre(request.nombre());
        p.setDescripcion(request.descripcion());
        p.setValor(request.valor());
        if (request.editable() != null) {
            p.setEditable(request.editable());
        }
        p.setUsuario(audit.actor());
        p.setEstacion(audit.estacion());
        return ParametroGeneralResponse.from(p);
    }

    private ParametroGeneral findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Parámetro no encontrado: " + id));
    }
}
