package com.contabilidad.reporte.rest;

import com.contabilidad.reporte.dto.EstructuraReporteResumen;
import com.contabilidad.reporte.dto.ReporteLineaRequest;
import com.contabilidad.reporte.dto.ReporteLineaResponse;
import com.contabilidad.reporte.service.EstructuraReporteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/** ABM de la plantilla de los reportes financieros (líneas del ESF / ER). */
@Path("/estructuras-reporte")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class EstructuraReporteResource {

    @Inject
    EstructuraReporteService service;

    @GET
    public List<EstructuraReporteResumen> reportes() {
        return service.listarReportes();
    }

    @GET
    @Path("/{codigo}/lineas")
    public List<ReporteLineaResponse> lineas(@PathParam("codigo") String codigo) {
        return service.lineasDe(codigo);
    }

    @POST
    @Path("/{codigo}/lineas")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response crear(@PathParam("codigo") String codigo, @Valid ReporteLineaRequest request) {
        return Response.status(Response.Status.CREATED).entity(service.crear(codigo, request)).build();
    }

    @PUT
    @Path("/lineas/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public ReporteLineaResponse actualizar(@PathParam("id") Long id, @Valid ReporteLineaRequest request) {
        return service.actualizar(id, request);
    }

    @DELETE
    @Path("/lineas/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response eliminar(@PathParam("id") Long id) {
        service.eliminar(id);
        return Response.noContent().build();
    }
}
