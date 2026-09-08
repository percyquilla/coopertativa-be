package com.bien.moneda.repository;

import com.bien.moneda.entity.Moneda;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonedaRepository implements PanacheRepositoryBase<Moneda, Long> {

    public java.util.Optional<Moneda> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }

    public boolean existsByCodigoAndIdNot(String codigo, Long id) {
        return count("codigo = ?1 and id <> ?2", codigo, id) > 0;
    }
}
