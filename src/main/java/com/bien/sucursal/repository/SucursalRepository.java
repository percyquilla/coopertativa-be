package com.bien.sucursal.repository;

import com.bien.sucursal.entity.Sucursal;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SucursalRepository implements PanacheRepositoryBase<Sucursal, Long> {

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }

    public boolean existsByCodigoAndIdNot(String codigo, Long id) {
        return count("codigo = ?1 and id <> ?2", codigo, id) > 0;
    }

    public long countVigentesExcepto(Long id) {
        return count("estado = 'V' and id <> ?1", id);
    }
}
