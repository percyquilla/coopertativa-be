package com.bien.usuario.repository;

import com.bien.usuario.entity.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<Usuario, Long> {

    public Optional<Usuario> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByPersonaId(Long personaId) {
        return count("personaId", personaId) > 0;
    }

    public boolean existsByPersonaIdAndIdNot(Long personaId, Long id) {
        return count("personaId = ?1 and id <> ?2", personaId, id) > 0;
    }
}
