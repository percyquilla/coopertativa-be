package com.bien.socio.repository;

import com.bien.socio.entity.Socio;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SocioRepository implements PanacheRepositoryBase<Socio, Long> {

    public boolean existsByPersonaId(Long personaId) {
        return count("personaId", personaId) > 0;
    }

    public boolean existsByNumeroSocio(String numeroSocio) {
        return count("numeroSocio", numeroSocio) > 0;
    }

    public boolean existsByNumeroSocioAndIdNot(String numeroSocio, Long id) {
        return count("numeroSocio = ?1 and id <> ?2", numeroSocio, id) > 0;
    }
}
