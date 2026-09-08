package com.bien.personaficha.repository;

import com.bien.personaficha.entity.PersonaContacto;
import com.bien.personaficha.entity.PersonaDireccion;
import com.bien.personaficha.entity.PersonaDocumento;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/** Los tres satélites de la ficha de una persona, con la misma forma de consulta. */
public final class PersonaFichaRepository {

    private PersonaFichaRepository() {}

    @ApplicationScoped
    public static class Direcciones implements PanacheRepositoryBase<PersonaDireccion, Long> {

        public List<PersonaDireccion> listByPersona(Long personaId) {
            return list("personaId = ?1 order by esPrincipal desc, id", personaId);
        }

        /** Deja una sola dirección principal por persona. */
        public void quitarPrincipal(Long personaId, Long excepto) {
            update("esPrincipal = false where personaId = ?1 and id <> ?2 and esPrincipal = true",
                    personaId, excepto == null ? -1L : excepto);
        }
    }

    @ApplicationScoped
    public static class Contactos implements PanacheRepositoryBase<PersonaContacto, Long> {

        public List<PersonaContacto> listByPersona(Long personaId) {
            return list("personaId = ?1 order by tipo, esPrincipal desc, id", personaId);
        }

        /** Un principal por tipo: el celular principal y el correo principal conviven. */
        public void quitarPrincipal(Long personaId, String tipo, Long excepto) {
            update("esPrincipal = false where personaId = ?1 and tipo = ?2 and id <> ?3 "
                            + "and esPrincipal = true",
                    personaId, tipo, excepto == null ? -1L : excepto);
        }
    }

    @ApplicationScoped
    public static class Documentos implements PanacheRepositoryBase<PersonaDocumento, Long> {

        public List<PersonaDocumento> listByPersona(Long personaId) {
            return list("personaId = ?1 order by tipo, id", personaId);
        }
    }
}
