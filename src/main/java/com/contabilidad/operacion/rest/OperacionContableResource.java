package com.contabilidad.operacion.rest;

import com.contabilidad.operacion.dto.OperacionContableResponse;
import com.contabilidad.operacion.service.OperacionContableService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/operaciones-contables")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class OperacionContableResource {

    @Inject
    OperacionContableService service;

    @GET
    public List<OperacionContableResponse> listAll(@QueryParam("estado") String estado) {
        return service.listAll(estado);
    }

    @GET
    @Path("/pendientes/cantidad")
    public Map<String, Long> cantidadPendientes() {
        return Map.of("pendientes", service.contarPendientes());
    }

    @GET
    @Path("/{id}")
    public OperacionContableResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @Path("/{id}/reprocesar")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public OperacionContableResponse reprocesar(@PathParam("id") Long id) {
        return service.reprocesar(id);
    }
}
