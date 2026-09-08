package com.bien.persona.repository;

import com.bien.persona.entity.Persona;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonaRepository implements PanacheRepositoryBase<Persona, Long> {

    public boolean existsByCedulaIdentidadAndExpedicion(String cedulaIdentidad, String expedicion) {
        return count("cedulaIdentidad = ?1 and expedicion = ?2", cedulaIdentidad, expedicion) > 0;
    }

    public boolean existsByCedulaIdentidadAndExpedicionAndIdNot(String cedulaIdentidad, String expedicion, Long id) {
        return count("cedulaIdentidad = ?1 and expedicion = ?2 and id <> ?3", cedulaIdentidad, expedicion, id) > 0;
    }
}
