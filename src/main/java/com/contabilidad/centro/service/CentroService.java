package com.contabilidad.centro.service;

import com.contabilidad.centro.dto.CentroRequest;
import com.contabilidad.centro.dto.CentroResponse;
import com.contabilidad.centro.entity.Centro;
import com.contabilidad.centro.repository.CentroRepository;
import com.bien.common.security.AuditContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class CentroService {

    @Inject
    CentroRepository repository;

    @Inject
    AuditContext audit;

    public List<CentroResponse> listAll() {
        return repository.list("order by codigo").stream()
                .map(CentroResponse::from)
                .toList();
    }

    public CentroResponse findById(Long id) {
        return CentroResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public CentroResponse create(CentroRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw conflicto("Ya existe un centro de costo con el código " + request.codigo() + ".");
        }
        Centro c = new Centro();
        c.setCodigo(request.codigo());
        c.setNombre(request.nombre());
        c.setEstado(request.estado() != null ? request.estado() : "V");
        c.setUsuario(audit.actor());
        c.setEstacion(audit.estacion());

        repository.persist(c);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(c);
        return CentroResponse.from(c);
    }

    @Transactional
    public CentroResponse update(Long id, CentroRequest request) {
        Centro c = findEntityOrThrow(id);
        if (repository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw conflicto("Ya existe un centro de costo con el código " + request.codigo() + ".");
        }
        String estado = request.estado() != null ? request.estado() : c.getEstado();
        // las cuentas afectables se abren por centro: sin ninguno vigente no se puede abrir ninguna
        if ("S".equals(estado) && !"S".equals(c.getEstado()) && repository.countVigentesExcepto(id) == 0) {
            throw conflicto("No se puede suspender el único centro de costo vigente.");
        }
        c.setCodigo(request.codigo());
        c.setNombre(request.nombre());
        c.setEstado(estado);
        c.setUsuario(audit.actor());
        c.setEstacion(audit.estacion());
        return CentroResponse.from(c);
    }

    public Centro findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Centro de costo no encontrado: " + id));
    }

    private WebApplicationException conflicto(String mensaje) {
        return new WebApplicationException(mensaje, Response.Status.CONFLICT);
    }
}
