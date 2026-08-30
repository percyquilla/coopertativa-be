package com.bien.persona.rest;

import com.bien.persona.dto.PersonaRequest;
import com.bien.persona.dto.PersonaResponse;
import com.bien.persona.service.PersonaService;
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

@Path("/personas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class PersonaResource {

    @Inject
    PersonaService personaService;

    @GET
    public List<PersonaResponse> listAll() {
        return personaService.listAll();
    }

    @GET
    @Path("/{id}")
    public PersonaResponse findById(@PathParam("id") Long id) {
        return personaService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid PersonaRequest request, @Context UriInfo uriInfo) {
        PersonaResponse response = personaService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public PersonaResponse update(@PathParam("id") Long id, @Valid PersonaRequest request) {
        return personaService.update(id, request);
    }
}
