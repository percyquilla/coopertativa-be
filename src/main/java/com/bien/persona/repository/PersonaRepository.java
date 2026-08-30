package com.bien.persona.repository;

import com.bien.persona.entity.Persona;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonaRepository implements PanacheRepositoryBase<Persona, Long> {
}
