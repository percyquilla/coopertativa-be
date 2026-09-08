package com.contabilidad.esquema.rest;

import com.contabilidad.esquema.dto.EsquemaContableRequest;
import com.contabilidad.esquema.dto.EsquemaContableResponse;
import com.contabilidad.esquema.service.EsquemaContableService;
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

@Path("/esquemas-contables")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class EsquemaContableResource {

    @Inject
    EsquemaContableService service;

    @GET
    public List<EsquemaContableResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public EsquemaContableResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid EsquemaContableRequest request, @Context UriInfo uriInfo) {
        EsquemaContableResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public EsquemaContableResponse update(@PathParam("id") Long id, @Valid EsquemaContableRequest request) {
        return service.update(id, request);
    }

    @POST
    @Path("/{id}/nueva-version")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public EsquemaContableResponse nuevaVersion(@PathParam("id") Long id) {
        return service.nuevaVersion(id);
    }
}
