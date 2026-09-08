package com.bien.parametros.rest;

import com.bien.parametros.dto.ParametroGeneralRequest;
import com.bien.parametros.dto.ParametroGeneralResponse;
import com.bien.parametros.dto.ParametroGeneralUpdateRequest;
import com.bien.parametros.service.ParametroGeneralService;
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

@Path("/parametros-generales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class ParametroGeneralResource {

    @Inject
    ParametroGeneralService parametroGeneralService;

    @GET
    public List<ParametroGeneralResponse> listAll() {
        return parametroGeneralService.listAll();
    }

    @GET
    @Path("/{id}")
    public ParametroGeneralResponse findById(@PathParam("id") Long id) {
        return parametroGeneralService.findById(id);
    }

    @GET
    @Path("/codigo/{codigo}")
    public ParametroGeneralResponse findByCodigo(@PathParam("codigo") String codigo) {
        return parametroGeneralService.findByCodigo(codigo);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid ParametroGeneralRequest request, @Context UriInfo uriInfo) {
        ParametroGeneralResponse response = parametroGeneralService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public ParametroGeneralResponse update(@PathParam("id") Long id, @Valid ParametroGeneralUpdateRequest request) {
        return parametroGeneralService.update(id, request);
    }
}
