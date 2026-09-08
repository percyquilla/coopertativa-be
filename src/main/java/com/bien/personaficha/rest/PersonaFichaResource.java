package com.bien.personaficha.rest;

import com.bien.personaficha.dto.PersonaFichaDtos.ContactoRequest;
import com.bien.personaficha.dto.PersonaFichaDtos.ContactoResponse;
import com.bien.personaficha.dto.PersonaFichaDtos.DireccionRequest;
import com.bien.personaficha.dto.PersonaFichaDtos.DireccionResponse;
import com.bien.personaficha.dto.PersonaFichaDtos.DocumentoRequest;
import com.bien.personaficha.dto.PersonaFichaDtos.DocumentoResponse;
import com.bien.personaficha.service.PersonaFichaService;
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

/** Ficha ampliada de una persona: direcciones, contactos y documentos. */
@Path("/personas/{personaId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "OFICIAL", "CAJERO", "GERENTE", "AUDITOR"})
public class PersonaFichaResource {

    private static final String ESCRITURA_ADMIN = "ADMIN";
    private static final String ESCRITURA_OFICIAL = "OFICIAL";

    @Inject
    PersonaFichaService service;

    // ---------- direcciones ----------

    @GET
    @Path("/direcciones")
    public List<DireccionResponse> listDirecciones(@PathParam("personaId") Long personaId) {
        return service.listDirecciones(personaId);
    }

    @POST
    @Path("/direcciones")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public Response crearDireccion(@PathParam("personaId") Long personaId,
                                   @Valid DireccionRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(service.crearDireccion(personaId, request)).build();
    }

    @PUT
    @Path("/direcciones/{id}")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public DireccionResponse actualizarDireccion(@PathParam("personaId") Long personaId,
                                                 @PathParam("id") Long id,
                                                 @Valid DireccionRequest request) {
        return service.actualizarDireccion(personaId, id, request);
    }

    @DELETE
    @Path("/direcciones/{id}")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public Response borrarDireccion(@PathParam("personaId") Long personaId,
                                    @PathParam("id") Long id) {
        service.borrarDireccion(personaId, id);
        return Response.noContent().build();
    }

    // ---------- contactos ----------

    @GET
    @Path("/contactos")
    public List<ContactoResponse> listContactos(@PathParam("personaId") Long personaId) {
        return service.listContactos(personaId);
    }

    @POST
    @Path("/contactos")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public Response crearContacto(@PathParam("personaId") Long personaId,
                                  @Valid ContactoRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(service.crearContacto(personaId, request)).build();
    }

    @PUT
    @Path("/contactos/{id}")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public ContactoResponse actualizarContacto(@PathParam("personaId") Long personaId,
                                               @PathParam("id") Long id,
                                               @Valid ContactoRequest request) {
        return service.actualizarContacto(personaId, id, request);
    }

    @DELETE
    @Path("/contactos/{id}")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public Response borrarContacto(@PathParam("personaId") Long personaId,
                                   @PathParam("id") Long id) {
        service.borrarContacto(personaId, id);
        return Response.noContent().build();
    }

    // ---------- documentos ----------

    @GET
    @Path("/documentos")
    public List<DocumentoResponse> listDocumentos(@PathParam("personaId") Long personaId) {
        return service.listDocumentos(personaId);
    }

    @POST
    @Path("/documentos")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public Response crearDocumento(@PathParam("personaId") Long personaId,
                                   @Valid DocumentoRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(service.crearDocumento(personaId, request)).build();
    }

    @PUT
    @Path("/documentos/{id}")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public DocumentoResponse actualizarDocumento(@PathParam("personaId") Long personaId,
                                                 @PathParam("id") Long id,
                                                 @Valid DocumentoRequest request) {
        return service.actualizarDocumento(personaId, id, request);
    }

    @DELETE
    @Path("/documentos/{id}")
    @RolesAllowed({ESCRITURA_ADMIN, ESCRITURA_OFICIAL})
    public Response borrarDocumento(@PathParam("personaId") Long personaId,
                                    @PathParam("id") Long id) {
        service.borrarDocumento(personaId, id);
        return Response.noContent().build();
    }
}
