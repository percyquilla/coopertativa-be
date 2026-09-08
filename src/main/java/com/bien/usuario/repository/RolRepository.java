package com.bien.usuario.repository;

import com.bien.usuario.entity.Rol;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class RolRepository implements PanacheRepositoryBase<Rol, Long> {

    public List<Rol> listByCodigoIn(Collection<String> codigos) {
        return list("codigo in ?1", codigos);
    }

    public List<Rol> listByIdIn(Collection<Long> ids) {
        return list("id in ?1", ids);
    }
}
