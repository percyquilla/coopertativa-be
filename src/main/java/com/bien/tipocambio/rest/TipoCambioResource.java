package com.bien.tipocambio.rest;

import com.bien.tipocambio.dto.TipoCambioRequest;
import com.bien.tipocambio.dto.TipoCambioResponse;
import com.bien.tipocambio.service.TipoCambioService;
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

@Path("/tipos-cambio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "CAJERO", "GERENTE", "AUDITOR"})
public class TipoCambioResource {

    @Inject
    TipoCambioService tipoCambioService;

    @GET
    public List<TipoCambioResponse> listAll() {
        return tipoCambioService.listAll();
    }

    @GET
    @Path("/{id}")
    public TipoCambioResponse findById(@PathParam("id") Long id) {
        return tipoCambioService.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid TipoCambioRequest request, @Context UriInfo uriInfo) {
        TipoCambioResponse response = tipoCambioService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public TipoCambioResponse update(@PathParam("id") Long id, @Valid TipoCambioRequest request) {
        return tipoCambioService.update(id, request);
    }
}
