package com.contabilidad.clasificacioncuenta.service;

import com.contabilidad.clasificacioncuenta.dto.ClasificacionCuentaResponse;
import com.contabilidad.clasificacioncuenta.entity.ClasificacionCuenta;
import com.contabilidad.clasificacioncuenta.repository.ClasificacionCuentaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ClasificacionCuentaService {

    @Inject
    ClasificacionCuentaRepository repository;

    public List<ClasificacionCuentaResponse> listAll() {
        return repository.list("order by codigo").stream()
                .map(ClasificacionCuentaResponse::from)
                .toList();
    }

    public ClasificacionCuenta findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Clasificación de cuenta no encontrada: " + id));
    }
}
