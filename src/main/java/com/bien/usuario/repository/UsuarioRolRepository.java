package com.bien.usuario.repository;

import com.bien.usuario.entity.UsuarioRol;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UsuarioRolRepository implements PanacheRepositoryBase<UsuarioRol, Long> {

    public List<UsuarioRol> listVigentesByUsuarioId(Long usuarioId) {
        return list("usuarioId = ?1 and fechaFin is null", usuarioId);
    }
}
