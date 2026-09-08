package com.bien.usuario.rest;

import com.bien.usuario.dto.UsuarioPasswordRequest;
import com.bien.usuario.dto.UsuarioRequest;
import com.bien.usuario.dto.UsuarioResponse;
import com.bien.usuario.dto.UsuarioUpdateRequest;
import com.bien.usuario.service.UsuarioService;
import io.quarkus.security.Authenticated;
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
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    @Path("/me")
    @Authenticated
    public UsuarioResponse me(@Context SecurityContext securityContext) {
        return usuarioService.findByUsername(securityContext.getUserPrincipal().getName());
    }

    @PUT
    @Path("/me/password")
    @Authenticated
    public Response changeOwnPassword(@Valid UsuarioPasswordRequest request, @Context SecurityContext securityContext) {
        usuarioService.changeOwnPassword(securityContext.getUserPrincipal().getName(), request);
        return Response.noContent().build();
    }

    @GET
    public List<UsuarioResponse> listAll() {
        return usuarioService.listAll();
    }

    @GET
    @Path("/{id}")
    public UsuarioResponse findById(@PathParam("id") Long id) {
        return usuarioService.findById(id);
    }

    @POST
    public Response create(@Valid UsuarioRequest request, @Context UriInfo uriInfo) {
        UsuarioResponse response = usuarioService.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public UsuarioResponse update(@PathParam("id") Long id, @Valid UsuarioUpdateRequest request) {
        return usuarioService.update(id, request);
    }

    @PUT
    @Path("/{id}/activar")
    public UsuarioResponse activar(@PathParam("id") Long id) {
        return usuarioService.setActivo(id, true);
    }

    @PUT
    @Path("/{id}/suspender")
    public UsuarioResponse suspender(@PathParam("id") Long id) {
        return usuarioService.setActivo(id, false);
    }

    @PUT
    @Path("/{id}/password")
    public Response changePassword(@PathParam("id") Long id, @Valid UsuarioPasswordRequest request) {
        usuarioService.changePassword(id, request);
        return Response.noContent().build();
    }
}
