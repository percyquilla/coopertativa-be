package com.bien.parametros.service;

import com.bien.parametros.dto.FeriadoRequest;
import com.bien.parametros.dto.FeriadoResponse;
import com.bien.parametros.entity.Feriado;
import com.bien.parametros.repository.FeriadoRepository;
import com.bien.sucursal.repository.SucursalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class FeriadoService {

    @Inject
    AuditContext audit;

    @Inject
    FeriadoRepository repository;

    @Inject
    SucursalRepository sucursalRepository;

    public List<FeriadoResponse> listAll() {
        return repository.list("order by fecha").stream()
                .map(FeriadoResponse::from)
                .toList();
    }

    public FeriadoResponse findById(Long id) {
        return FeriadoResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public FeriadoResponse create(FeriadoRequest request) {
        validateSucursal(request.sucursalId());
        validateNoDuplicate(request, null);

        Feriado f = new Feriado();
        f.setFecha(request.fecha());
        f.setDescripcion(request.descripcion());
        f.setAmbito(request.ambito());
        f.setSucursalId(request.sucursalId());
        f.setRecurrente(request.recurrente() != null && request.recurrente());
        f.setEstado("V");
        f.setUsuario(audit.actor());
        f.setEstacion("SISTEMA");
        repository.persist(f);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(f);
        return FeriadoResponse.from(f);
    }

    @Transactional
    public FeriadoResponse update(Long id, FeriadoRequest request) {
        Feriado f = findEntityOrThrow(id);
        validateSucursal(request.sucursalId());
        validateNoDuplicate(request, id);

        f.setFecha(request.fecha());
        f.setDescripcion(request.descripcion());
        f.setAmbito(request.ambito());
        f.setSucursalId(request.sucursalId());
        if (request.recurrente() != null) {
            f.setRecurrente(request.recurrente());
        }
        f.setUsuario(audit.actor());
        return FeriadoResponse.from(f);
    }

    @Transactional
    public void delete(Long id) {
        Feriado f = findEntityOrThrow(id);
        repository.delete(f);
    }

    private void validateSucursal(Long sucursalId) {
        if (sucursalId != null && sucursalRepository.findByIdOptional(sucursalId).isEmpty()) {
            throw new NotFoundException("Sucursal no encontrada: " + sucursalId);
        }
    }

    private void validateNoDuplicate(FeriadoRequest request, Long id) {
        boolean duplicate;
        if (request.sucursalId() == null) {
            duplicate = id == null
                    ? repository.existsNacional(request.fecha())
                    : repository.existsNacionalAndIdNot(request.fecha(), id);
        } else {
            duplicate = id == null
                    ? repository.existsSucursal(request.fecha(), request.sucursalId())
                    : repository.existsSucursalAndIdNot(request.fecha(), request.sucursalId(), id);
        }
        if (duplicate) {
            throw new WebApplicationException("Ya existe un feriado para esa fecha", Response.Status.CONFLICT);
        }
    }

    private Feriado findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Feriado no encontrado: " + id));
    }
}
