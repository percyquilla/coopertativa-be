package com.bien.moneda.service;

import com.bien.moneda.dto.MonedaRequest;
import com.bien.moneda.dto.MonedaResponse;
import com.bien.moneda.entity.Moneda;
import com.bien.moneda.repository.MonedaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class MonedaService {

    @Inject
    MonedaRepository monedaRepository;

    public List<MonedaResponse> listAll() {
        return monedaRepository.list("order by codigo").stream()
                .map(MonedaResponse::from)
                .toList();
    }

    public MonedaResponse findById(Long id) {
        return MonedaResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public MonedaResponse create(MonedaRequest request) {
        if (monedaRepository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("El código de moneda ya existe: " + request.codigo(), Response.Status.CONFLICT);
        }
        Moneda m = new Moneda();
        m.setCodigo(request.codigo());
        m.setDescripcion(request.descripcion());
        m.setSimbolo(request.simbolo());
        m.setEstado(request.estado() != null ? request.estado() : "V");
        monedaRepository.persist(m);
        monedaRepository.getEntityManager().flush();
        monedaRepository.getEntityManager().refresh(m);
        return MonedaResponse.from(m);
    }

    @Transactional
    public MonedaResponse update(Long id, MonedaRequest request) {
        Moneda m = findEntityOrThrow(id);
        if (monedaRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new WebApplicationException("El código de moneda ya existe: " + request.codigo(), Response.Status.CONFLICT);
        }
        m.setCodigo(request.codigo());
        m.setDescripcion(request.descripcion());
        m.setSimbolo(request.simbolo());
        if (request.estado() != null) {
            m.setEstado(request.estado());
        }
        return MonedaResponse.from(m);
    }

    private Moneda findEntityOrThrow(Long id) {
        return monedaRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Moneda no encontrada: " + id));
    }
}
