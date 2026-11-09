package com.bien.certificadoaportacion.rest;

import com.bien.certificadoaportacion.dto.CertificadoAportacionRequest;
import com.bien.certificadoaportacion.dto.CertificadoAportacionResponse;
import com.bien.certificadoaportacion.dto.ValoresEmisionResponse;
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

/**
 * Certificados de aportación. Registrar, aprobar, modificar y eliminar:
 * OFICIAL (o ADMIN). Autorizar (contabiliza) y rechazar: GERENTE (o ADMIN).
 */
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
    @Path("/valores-emision")
    public ValoresEmisionResponse valoresEmision() {
        return service.valoresEmision();
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

    @GET
    @Path("/socio/{socioId}/vigentes")
    public List<CertificadoAportacionResponse> listVigentesBySocio(@PathParam("socioId") Long socioId) {
        return service.listVigentesBySocio(socioId);
    }

    @POST
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public Response create(@Valid CertificadoAportacionRequest request, @Context UriInfo uriInfo) {
        CertificadoAportacionResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    /** Modificar un certificado rechazado: vuelve a REG. */
    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public CertificadoAportacionResponse update(@PathParam("id") Long id,
                                                @Valid CertificadoAportacionRequest request) {
        return service.update(id, request);
    }

    /** Eliminar (lógico, estado ELM) un certificado registrado o rechazado. */
    @PUT
    @Path("/{id}/eliminar")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public CertificadoAportacionResponse eliminar(@PathParam("id") Long id) {
        return service.eliminar(id);
    }

    @PUT
    @Path("/{id}/aprobar")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public CertificadoAportacionResponse aprobar(@PathParam("id") Long id) {
        return service.aprobar(id);
    }

    @PUT
    @Path("/{id}/autorizar")
    @RolesAllowed({"ADMIN", "GERENTE"})
    public CertificadoAportacionResponse autorizar(@PathParam("id") Long id) {
        return service.autorizar(id);
    }

    @PUT
    @Path("/{id}/rechazar")
    @RolesAllowed({"ADMIN", "GERENTE"})
    public CertificadoAportacionResponse rechazar(@PathParam("id") Long id) {
        return service.rechazar(id);
    }
}
