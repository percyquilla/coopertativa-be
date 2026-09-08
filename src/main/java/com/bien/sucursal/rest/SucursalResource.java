package com.bien.sucursal.rest;

import com.bien.sucursal.dto.SucursalRequest;
import com.bien.sucursal.dto.SucursalResponse;
import com.bien.sucursal.service.SucursalService;
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

@Path("/sucursales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "OFICIAL", "CAJERO", "CONTADOR", "GERENTE", "AUDITOR"})
public class SucursalResource {

    @Inject
    SucursalService sucursalService;

    @GET
    public List<SucursalResponse> listAll() {
        return sucursalService.listAll();
    }

    @GET
    @Path("/{id}")
    public SucursalResponse findById(@PathParam("id") Long id) {
        return sucursalService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid SucursalRequest request, @Context UriInfo uriInfo) {
        SucursalResponse response = sucursalService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public SucursalResponse update(@PathParam("id") Long id, @Valid SucursalRequest request) {
        return sucursalService.update(id, request);
    }
}
