package com.bien.parametros.repository;

import com.bien.parametros.entity.Institucion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class InstitucionRepository implements PanacheRepositoryBase<Institucion, Long> {

    public Optional<Institucion> findSingle() {
        return find("order by id").firstResultOptional();
    }
}
