package com.contabilidad.centro.rest;

import com.contabilidad.centro.dto.CentroRequest;
import com.contabilidad.centro.dto.CentroResponse;
import com.contabilidad.centro.service.CentroService;
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

@Path("/centros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class CentroResource {

    @Inject
    CentroService centroService;

    @GET
    public List<CentroResponse> listAll() {
        return centroService.listAll();
    }

    @GET
    @Path("/{id}")
    public CentroResponse findById(@PathParam("id") Long id) {
        return centroService.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid CentroRequest request, @Context UriInfo uriInfo) {
        CentroResponse response = centroService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public CentroResponse update(@PathParam("id") Long id, @Valid CentroRequest request) {
        return centroService.update(id, request);
    }
}
