package com.bien.producto.rest;

import com.bien.producto.dto.ProductoRequest;
import com.bien.producto.dto.ProductoResponse;
import com.bien.producto.service.ProductoService;
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

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CAJERO", "OFICIAL", "CONTADOR", "GERENTE", "AUDITOR"})
public class ProductoResource {

    @Inject
    ProductoService productoService;

    @GET
    public List<ProductoResponse> listAll() {
        return productoService.listAll();
    }

    @GET
    @Path("/{id}")
    public ProductoResponse findById(@PathParam("id") Long id) {
        return productoService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid ProductoRequest request, @Context UriInfo uriInfo) {
        ProductoResponse response = productoService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public ProductoResponse update(@PathParam("id") Long id, @Valid ProductoRequest request) {
        return productoService.update(id, request);
    }
}
