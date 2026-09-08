package com.bien.sucursal.service;

import com.bien.sucursal.dto.SucursalRequest;
import com.bien.sucursal.dto.SucursalResponse;
import com.bien.sucursal.entity.Sucursal;
import com.bien.sucursal.repository.SucursalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class SucursalService {

    @Inject
    SucursalRepository sucursalRepository;

    public List<SucursalResponse> listAll() {
        return sucursalRepository.list("order by codigo").stream()
                .map(SucursalResponse::from)
                .toList();
    }

    public SucursalResponse findById(Long id) {
        return SucursalResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public SucursalResponse create(SucursalRequest request) {
        if (sucursalRepository.existsByCodigo(request.codigo())) {
            throw new WebApplicationException("El código de sucursal ya existe: " + request.codigo(), Response.Status.CONFLICT);
        }
        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo(request.codigo());
        sucursal.setNombre(request.nombre());
        sucursal.setEstado(request.estado() != null ? request.estado() : "V");
        sucursalRepository.persist(sucursal);
        sucursalRepository.getEntityManager().flush();
        sucursalRepository.getEntityManager().refresh(sucursal);
        return SucursalResponse.from(sucursal);
    }

    @Transactional
    public SucursalResponse update(Long id, SucursalRequest request) {
        Sucursal sucursal = findEntityOrThrow(id);
        if (sucursalRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new WebApplicationException("El código de sucursal ya existe: " + request.codigo(), Response.Status.CONFLICT);
        }
        String estado = request.estado() != null ? request.estado() : sucursal.getEstado();
        // sin ninguna sucursal vigente no se pueden dar de alta socios ni usuarios
        if ("S".equals(estado) && !"S".equals(sucursal.getEstado())
                && sucursalRepository.countVigentesExcepto(id) == 0) {
            throw new WebApplicationException(
                    "No se puede suspender la única sucursal vigente: sin sucursales no se pueden "
                            + "registrar socios ni usuarios.",
                    Response.Status.CONFLICT);
        }
        sucursal.setCodigo(request.codigo());
        sucursal.setNombre(request.nombre());
        sucursal.setEstado(estado);
        return SucursalResponse.from(sucursal);
    }

    private Sucursal findEntityOrThrow(Long id) {
        return sucursalRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada: " + id));
    }
}
