package com.contabilidad.cuentacontable.rest;

import com.contabilidad.cuentacontable.dto.CuentaContableRequest;
import com.contabilidad.cuentacontable.dto.CuentaContableResponse;
import com.contabilidad.cuentacontable.dto.CuentaContableUpdateRequest;
import com.contabilidad.cuentacontable.service.CuentaContableService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/cuentas-contables")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class CuentaContableResource {

    @Inject
    CuentaContableService service;

    @GET
    public List<CuentaContableResponse> listAll() {
        return service.listAll();
    }

    /** Cuentas de mayor vigentes: las únicas contra las que se puede asentar. */
    @GET
    @Path("/imputables")
    public List<CuentaContableResponse> listImputables() {
        return service.listImputables();
    }

    @GET
    @Path("/{id}")
    public CuentaContableResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid CuentaContableRequest request, @Context UriInfo uriInfo) {
        CuentaContableResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public CuentaContableResponse update(@PathParam("id") Long id, @Valid CuentaContableUpdateRequest request) {
        return service.update(id, request);
    }
}
