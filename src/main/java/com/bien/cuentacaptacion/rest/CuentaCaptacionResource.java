package com.bien.cuentacaptacion.rest;

import com.bien.cuentacaptacion.dto.CuentaCaptacionRequest;
import com.bien.cuentacaptacion.dto.CuentaCaptacionResponse;
import com.bien.cuentacaptacion.service.CuentaCaptacionService;
import com.bien.cuentatitular.dto.CuentaTitularRequest;
import com.bien.cuentatitular.dto.CuentaTitularResponse;
import com.bien.cuentatitular.service.CuentaTitularService;
import com.bien.movimiento.dto.MovimientoRequest;
import com.bien.movimiento.dto.MovimientoResponse;
import com.bien.movimiento.service.MovimientoService;
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

@Path("/cuentas-captacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "GERENTE", "AUDITOR"})
public class CuentaCaptacionResource {

    @Inject
    CuentaCaptacionService cuentaService;

    @Inject
    CuentaTitularService titularService;

    @Inject
    MovimientoService movimientoService;

    // ---- cuenta ----

    @GET
    public List<CuentaCaptacionResponse> listAll() {
        return cuentaService.listAll();
    }

    @GET
    @Path("/{id}")
    public CuentaCaptacionResponse findById(@PathParam("id") Long id) {
        return cuentaService.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CAJERO"})
    public Response create(@Valid CuentaCaptacionRequest request, @Context UriInfo uriInfo) {
        CuentaCaptacionResponse response = cuentaService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}/cerrar")
    @RolesAllowed({"ADMIN", "CAJERO"})
    public CuentaCaptacionResponse cerrar(@PathParam("id") Long id) {
        return cuentaService.cerrar(id);
    }

    // ---- titulares ----

    @GET
    @Path("/{id}/titulares")
    public List<CuentaTitularResponse> listTitulares(@PathParam("id") Long id) {
        return titularService.listByCuenta(id);
    }

    @POST
    @Path("/{id}/titulares")
    @RolesAllowed({"ADMIN", "CAJERO"})
    public Response agregarTitular(@PathParam("id") Long id, @Valid CuentaTitularRequest request) {
        return Response.status(Response.Status.CREATED).entity(titularService.agregar(id, request)).build();
    }

    @DELETE
    @Path("/{id}/titulares/{titularId}")
    @RolesAllowed({"ADMIN", "CAJERO"})
    public CuentaTitularResponse finalizarTitular(@PathParam("id") Long id, @PathParam("titularId") Long titularId) {
        return titularService.finalizar(id, titularId);
    }

    // ---- movimientos ----

    @GET
    @Path("/{id}/movimientos")
    public List<MovimientoResponse> listMovimientos(@PathParam("id") Long id) {
        return movimientoService.listByCuenta(id);
    }

    @POST
    @Path("/{id}/movimientos")
    @RolesAllowed({"ADMIN", "CAJERO"})
    public Response registrarMovimiento(@PathParam("id") Long id, @Valid MovimientoRequest request) {
        return Response.status(Response.Status.CREATED).entity(movimientoService.registrar(id, request)).build();
    }
}
