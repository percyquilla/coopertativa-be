package com.bien.solicitudretiro.rest;

import com.bien.solicitudretiro.dto.ResolucionRequest;
import com.bien.solicitudretiro.dto.SolicitudRetiroRequest;
import com.bien.solicitudretiro.dto.SolicitudRetiroResponse;
import com.bien.solicitudretiro.service.SolicitudRetiroService;
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

@Path("/solicitudes-retiro")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "GERENTE", "AUDITOR"})
public class SolicitudRetiroResource {

    @Inject
    SolicitudRetiroService service;

    @GET
    public List<SolicitudRetiroResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public SolicitudRetiroResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @GET
    @Path("/socio/{socioId}")
    public List<SolicitudRetiroResponse> listBySocio(@PathParam("socioId") Long socioId) {
        return service.listBySocio(socioId);
    }

    @POST
    @RolesAllowed({"ADMIN", "CAJERO", "OFICIAL"})
    public Response create(@Valid SolicitudRetiroRequest request, @Context UriInfo uriInfo) {
        SolicitudRetiroResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}/aprobar")
    @RolesAllowed({"ADMIN", "GERENTE"})
    public SolicitudRetiroResponse aprobar(@PathParam("id") Long id, @Valid ResolucionRequest request) {
        return service.aprobar(id, request);
    }

    @PUT
    @Path("/{id}/rechazar")
    @RolesAllowed({"ADMIN", "GERENTE"})
    public SolicitudRetiroResponse rechazar(@PathParam("id") Long id, @Valid ResolucionRequest request) {
        return service.rechazar(id, request);
    }

    @PUT
    @Path("/{id}/pagar")
    @RolesAllowed({"ADMIN", "GERENTE"})
    public SolicitudRetiroResponse pagar(@PathParam("id") Long id, @Valid ResolucionRequest request) {
        return service.pagar(id, request);
    }

    @PUT
    @Path("/{id}/anular")
    @RolesAllowed({"ADMIN", "GERENTE"})
    public SolicitudRetiroResponse anular(@PathParam("id") Long id, @Valid ResolucionRequest request) {
        return service.anular(id, request);
    }
}
