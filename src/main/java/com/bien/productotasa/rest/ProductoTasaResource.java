package com.bien.productotasa.rest;

import com.bien.productotasa.dto.ProductoTasaRequest;
import com.bien.productotasa.dto.ProductoTasaResponse;
import com.bien.productotasa.service.ProductoTasaService;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/productos/{productoId}/tasas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "CONTADOR", "GERENTE", "AUDITOR"})
public class ProductoTasaResource {

    @Inject
    ProductoTasaService service;

    @GET
    public List<ProductoTasaResponse> listByProducto(@PathParam("productoId") Long productoId) {
        return service.listByProducto(productoId);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@PathParam("productoId") Long productoId,
                           @Valid ProductoTasaRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(productoId, request))
                .build();
    }

    @PUT
    @Path("/{tasaId}")
    @RolesAllowed("ADMIN")
    public ProductoTasaResponse update(@PathParam("productoId") Long productoId,
                                       @PathParam("tasaId") Long tasaId,
                                       @Valid ProductoTasaRequest request) {
        return service.update(productoId, tasaId, request);
    }
}
