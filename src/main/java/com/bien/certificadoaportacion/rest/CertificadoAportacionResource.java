package com.bien.certificadoaportacion.rest;

import com.bien.certificadoaportacion.dto.CertificadoAportacionRequest;
import com.bien.certificadoaportacion.dto.CertificadoAportacionResponse;
import com.bien.certificadoaportacion.service.CertificadoAportacionService;
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

@Path("/certificados-aportacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "GERENTE", "AUDITOR"})
public class CertificadoAportacionResource {

    @Inject
    CertificadoAportacionService service;

    @GET
    public List<CertificadoAportacionResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public CertificadoAportacionResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @GET
    @Path("/socio/{socioId}")
    public List<CertificadoAportacionResponse> listBySocio(@PathParam("socioId") Long socioId) {
        return service.listBySocio(socioId);
    }

    @POST
    @RolesAllowed({"ADMIN", "CAJERO", "OFICIAL"})
    public Response create(@Valid CertificadoAportacionRequest request, @Context UriInfo uriInfo) {
        CertificadoAportacionResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}/anular")
    @RolesAllowed({"ADMIN", "CAJERO", "OFICIAL"})
    public CertificadoAportacionResponse anular(@PathParam("id") Long id) {
        return service.anular(id);
    }
}
