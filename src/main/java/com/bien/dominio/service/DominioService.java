package com.bien.dominio.service;

import com.bien.dominio.dto.DominioResponse;
import com.bien.dominio.entity.Dominio;
import com.bien.dominio.entity.DominioValor;
import com.bien.dominio.repository.DominioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DominioService {

    @Inject
    DominioRepository repository;

    @Inject
    DominioRepository.DominioValorRepository valorRepository;

    public List<DominioResponse> listAll() {
        Map<String, List<DominioValor>> valores = repository.valoresPorDominio();
        return repository.listOrdenados().stream()
                .map(d -> DominioResponse.from(d, valores.getOrDefault(d.getId(), List.of())))
                .toList();
    }

    public DominioResponse findById(String id) {
        Dominio d = repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Dominio no encontrado: " + id));
        return DominioResponse.from(d, valorRepository.listByDominio(id));
    }
}
