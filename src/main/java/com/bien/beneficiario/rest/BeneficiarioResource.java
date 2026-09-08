package com.bien.beneficiario.rest;

import com.bien.beneficiario.dto.BeneficiarioDtos.BeneficiarioRequest;
import com.bien.beneficiario.dto.BeneficiarioDtos.BeneficiarioResponse;
import com.bien.beneficiario.service.BeneficiarioService;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/socios/{socioId}/beneficiarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "OFICIAL", "CAJERO", "GERENTE", "AUDITOR"})
public class BeneficiarioResource {

    @Inject
    BeneficiarioService service;

    @GET
    public List<BeneficiarioResponse> listBySocio(@PathParam("socioId") Long socioId) {
        return service.listBySocio(socioId);
    }

    @POST
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public Response create(@PathParam("socioId") Long socioId, @Valid BeneficiarioRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(socioId, request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public BeneficiarioResponse update(@PathParam("socioId") Long socioId,
                                       @PathParam("id") Long id,
                                       @Valid BeneficiarioRequest request) {
        return service.update(socioId, id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "OFICIAL"})
    public Response delete(@PathParam("socioId") Long socioId, @PathParam("id") Long id) {
        service.delete(socioId, id);
        return Response.noContent().build();
    }
}
