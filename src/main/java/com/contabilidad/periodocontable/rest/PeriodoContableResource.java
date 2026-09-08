package com.contabilidad.periodocontable.rest;

import com.contabilidad.periodocontable.dto.PeriodoContableResponse;
import com.contabilidad.periodocontable.service.PeriodoContableService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/periodos-contables")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class PeriodoContableResource {

    @Inject
    PeriodoContableService service;

    @GET
    public List<PeriodoContableResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public PeriodoContableResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @Path("/gestion/{gestion}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response generarGestion(@PathParam("gestion") Integer gestion) {
        return Response.status(Response.Status.CREATED)
                .entity(service.generarGestion(gestion))
                .build();
    }

    @PUT
    @Path("/{id}/cerrar")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public PeriodoContableResponse cerrar(@PathParam("id") Long id) {
        return service.cerrar(id);
    }

    @PUT
    @Path("/{id}/reabrir")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public PeriodoContableResponse reabrir(@PathParam("id") Long id) {
        return service.reabrir(id);
    }
}
