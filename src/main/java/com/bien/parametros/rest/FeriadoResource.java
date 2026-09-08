package com.bien.parametros.rest;

import com.bien.parametros.dto.FeriadoRequest;
import com.bien.parametros.dto.FeriadoResponse;
import com.bien.parametros.service.FeriadoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

@Path("/feriados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class FeriadoResource {

    @Inject
    FeriadoService feriadoService;

    @GET
    public List<FeriadoResponse> listAll() {
        return feriadoService.listAll();
    }

    @GET
    @Path("/{id}")
    public FeriadoResponse findById(@PathParam("id") Long id) {
        return feriadoService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid FeriadoRequest request, @Context UriInfo uriInfo) {
        FeriadoResponse response = feriadoService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public FeriadoResponse update(@PathParam("id") Long id, @Valid FeriadoRequest request) {
        return feriadoService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        feriadoService.delete(id);
        return Response.noContent().build();
    }
}
