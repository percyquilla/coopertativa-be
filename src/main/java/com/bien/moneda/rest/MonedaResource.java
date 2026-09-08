package com.bien.moneda.rest;

import com.bien.moneda.dto.MonedaRequest;
import com.bien.moneda.dto.MonedaResponse;
import com.bien.moneda.service.MonedaService;
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

@Path("/monedas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "CAJERO", "GERENTE", "AUDITOR"})
public class MonedaResource {

    @Inject
    MonedaService monedaService;

    @GET
    public List<MonedaResponse> listAll() {
        return monedaService.listAll();
    }

    @GET
    @Path("/{id}")
    public MonedaResponse findById(@PathParam("id") Long id) {
        return monedaService.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid MonedaRequest request, @Context UriInfo uriInfo) {
        MonedaResponse response = monedaService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public MonedaResponse update(@PathParam("id") Long id, @Valid MonedaRequest request) {
        return monedaService.update(id, request);
    }
}
