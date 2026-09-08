package com.bien.dominio.repository;

import com.bien.dominio.entity.Dominio;
import com.bien.dominio.entity.DominioValor;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class DominioRepository implements PanacheRepositoryBase<Dominio, String> {

    @Inject
    DominioValorRepository valorRepository;

    public List<Dominio> listOrdenados() {
        return list("order by id");
    }

    /** Todos los valores agrupados por dominio, para armar la lista sin N+1. */
    public Map<String, List<DominioValor>> valoresPorDominio() {
        return valorRepository.list("order by dominioId, valor").stream()
                .collect(Collectors.groupingBy(DominioValor::getDominioId));
    }

    @ApplicationScoped
    public static class DominioValorRepository implements PanacheRepositoryBase<DominioValor, Long> {

        public List<DominioValor> listByDominio(String dominioId) {
            return list("dominioId = ?1 order by valor", dominioId);
        }
    }
}
