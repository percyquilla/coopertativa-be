package com.bien.dominio.rest;

import com.bien.dominio.dto.DominioResponse;
import com.bien.dominio.service.DominioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Catálogo de códigos, <strong>solo lectura</strong>: las validaciones y los
 * desplegables viven en el código, no acá. Agregar un valor por esta vía no
 * lo haría funcionar en ninguna pantalla, así que no se expone escritura.
 */
@Path("/dominios")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "CONTADOR", "GERENTE", "AUDITOR"})
public class DominioResource {

    @Inject
    DominioService dominioService;

    @GET
    public List<DominioResponse> listAll() {
        return dominioService.listAll();
    }

    @GET
    @Path("/{id}")
    public DominioResponse findById(@PathParam("id") String id) {
        return dominioService.findById(id);
    }
}
