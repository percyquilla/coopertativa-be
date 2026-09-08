package com.contabilidad.cuentaafectable.rest;

import com.contabilidad.cuentaafectable.dto.CuentaAfectableRequest;
import com.contabilidad.cuentaafectable.dto.CuentaAfectableResponse;
import com.contabilidad.cuentaafectable.dto.CuentaAfectableUpdateRequest;
import com.contabilidad.cuentaafectable.service.CuentaAfectableService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/cuentas-afectables")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class CuentaAfectableResource {

    @Inject
    CuentaAfectableService service;

    @GET
    public List<CuentaAfectableResponse> listAll(@QueryParam("cuentaId") Long cuentaId) {
        return cuentaId != null ? service.listByCuenta(cuentaId) : service.listAll();
    }

    @GET
    @Path("/{id}")
    public CuentaAfectableResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public Response create(@Valid CuentaAfectableRequest request, @Context UriInfo uriInfo) {
        CuentaAfectableResponse response = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public CuentaAfectableResponse update(@PathParam("id") Long id, @Valid CuentaAfectableUpdateRequest request) {
        return service.update(id, request);
    }
}
