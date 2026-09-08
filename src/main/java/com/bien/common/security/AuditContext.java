package com.bien.common.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

/**
 * Quién está ejecutando la petición actual, para estampar las columnas de
 * auditoría (`usuario`) de cada registro.
 * <p>
 * El nombre sale del JWT (claim {@code upn}), no del cuerpo del request: antes
 * los formularios mandaban este dato y casi siempre era {@code null}, así que no
 * había traza de quién hizo cada operación. Ahora los servicios lo toman de aquí
 * e ignoran lo que venga en el DTO.
 */
@RequestScoped
public class AuditContext {

    static final String SISTEMA = "SISTEMA";

    @Inject
    SecurityIdentity identity;

    /** Username del usuario autenticado, o {@code "SISTEMA"} si no hay identidad. */
    public String actor() {
        if (identity == null || identity.isAnonymous() || identity.getPrincipal() == null) {
            return SISTEMA;
        }
        String name = identity.getPrincipal().getName();
        return (name == null || name.isBlank()) ? SISTEMA : name;
    }
}
