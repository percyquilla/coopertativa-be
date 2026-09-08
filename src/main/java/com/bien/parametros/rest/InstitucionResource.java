package com.bien.parametros.rest;

import com.bien.parametros.dto.InstitucionRequest;
import com.bien.parametros.dto.InstitucionResponse;
import com.bien.parametros.service.InstitucionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/institucion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class InstitucionResource {

    @Inject
    InstitucionService institucionService;

    @GET
    public InstitucionResponse get() {
        return institucionService.get();
    }

    @PUT
    @RolesAllowed("ADMIN")
    public InstitucionResponse update(@Valid InstitucionRequest request) {
        return institucionService.update(request);
    }
}
