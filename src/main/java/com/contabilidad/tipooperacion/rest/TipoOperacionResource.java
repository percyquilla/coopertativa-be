package com.contabilidad.tipooperacion.rest;

import com.contabilidad.tipooperacion.dto.TipoOperacionRequest;
import com.contabilidad.tipooperacion.dto.TipoOperacionResponse;
import com.contabilidad.tipooperacion.service.TipoOperacionService;
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

@Path("/tipos-operacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class TipoOperacionResource {

    @Inject
    TipoOperacionService service;

    @GET
    public List<TipoOperacionResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public TipoOperacionResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid TipoOperacionRequest request, @Context UriInfo uriInfo) {
        TipoOperacionResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public TipoOperacionResponse update(@PathParam("id") Long id, @Valid TipoOperacionRequest request) {
        return service.update(id, request);
    }
}
