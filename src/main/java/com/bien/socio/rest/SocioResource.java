package com.bien.socio.rest;

import com.bien.socio.dto.SocioRequest;
import com.bien.socio.dto.SocioResponse;
import com.bien.socio.dto.SocioUpdateRequest;
import com.bien.socio.service.SocioService;
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

@Path("/socios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "OFICIAL", "CAJERO", "GERENTE", "AUDITOR"})
public class SocioResource {

    @Inject
    SocioService socioService;

    @GET
    public List<SocioResponse> listAll() {
        return socioService.listAll();
    }

    @GET
    @Path("/{id}")
    public SocioResponse findById(@PathParam("id") Long id) {
        return socioService.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public Response create(@Valid SocioRequest request, @Context UriInfo uriInfo) {
        SocioResponse response = socioService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public SocioResponse update(@PathParam("id") Long id, @Valid SocioUpdateRequest request) {
        return socioService.update(id, request);
    }

    @PUT
    @Path("/{id}/retirar")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public SocioResponse retirar(@PathParam("id") Long id) {
        return socioService.retirar(id);
    }

    @PUT
    @Path("/{id}/reactivar")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public SocioResponse reactivar(@PathParam("id") Long id) {
        return socioService.reactivar(id);
    }
}
