package com.contabilidad.comprobante.rest;

import com.contabilidad.comprobante.dto.ComprobanteRequest;
import com.contabilidad.comprobante.dto.ComprobanteResponse;
import com.contabilidad.comprobante.service.ComprobanteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/comprobantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class ComprobanteResource {

    @Inject
    ComprobanteService service;

    @GET
    public List<ComprobanteResponse> listAll(@QueryParam("periodoId") Long periodoId,
                                             @QueryParam("tipoComprobanteId") Long tipoComprobanteId,
                                             @QueryParam("estado") String estado,
                                             @QueryParam("desde") String desde,
                                             @QueryParam("hasta") String hasta) {
        return service.listAll(periodoId, tipoComprobanteId, estado, fecha(desde, "desde"), fecha(hasta, "hasta"));
    }

    @GET
    @Path("/{id}")
    public ComprobanteResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid ComprobanteRequest request, @Context UriInfo uriInfo) {
        ComprobanteResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public ComprobanteResponse update(@PathParam("id") Long id, @Valid ComprobanteRequest request) {
        return service.update(id, request);
    }

    @PUT
    @Path("/{id}/autorizar")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public ComprobanteResponse autorizar(@PathParam("id") Long id) {
        return service.autorizar(id);
    }

    /** Devuelve el comprobante de reversión cuando el original estaba autorizado. */
    @PUT
    @Path("/{id}/anular")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public ComprobanteResponse anular(@PathParam("id") Long id, @QueryParam("motivo") String motivo) {
        return service.anular(id, motivo);
    }

    // ---- internos ----

    private static LocalDate fecha(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La fecha «" + campo + "» debe tener el formato AAAA-MM-DD.");
        }
    }
}
