package com.bien.interes.rest;

import com.bien.interes.dto.InteresResponse;
import com.bien.interes.service.InteresService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Intereses de una cuenta de captación (solo lectura), para el detalle de la cuenta. */
@Path("/cuentas-captacion/{cuentaId}/intereses")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "GERENTE", "AUDITOR", "CONTADOR"})
public class InteresResource {

    @Inject
    InteresService service;

    @GET
    public InteresResponse resumen(@PathParam("cuentaId") Long cuentaId) {
        return service.resumenDe(cuentaId);
    }
}
