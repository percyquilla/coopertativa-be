package com.bien.parametros.repository;

import com.bien.parametros.entity.ParametroGeneral;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ParametroGeneralRepository implements PanacheRepositoryBase<ParametroGeneral, Long> {

    public Optional<ParametroGeneral> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }
}
