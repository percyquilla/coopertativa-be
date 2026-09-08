package com.contabilidad.diacontable.rest;

import com.contabilidad.diacontable.dto.DiaContableResponse;
import com.contabilidad.diacontable.service.DiaContableService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Path("/dias-contables")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class DiaContableResource {

    @Inject
    DiaContableService service;

    @GET
    @Path("/{fecha}")
    public DiaContableResponse estado(@PathParam("fecha") String fecha) {
        return service.estado(parse(fecha));
    }

    @PUT
    @Path("/{fecha}/abrir")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public DiaContableResponse abrir(@PathParam("fecha") String fecha) {
        return service.abrir(parse(fecha));
    }

    @PUT
    @Path("/{fecha}/cerrar")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public DiaContableResponse cerrar(@PathParam("fecha") String fecha) {
        return service.cerrar(parse(fecha));
    }

    @PUT
    @Path("/{fecha}/reabrir")
    @RolesAllowed({"ADMIN", "CONTADOR"})
    public DiaContableResponse reabrir(@PathParam("fecha") String fecha) {
        return service.reabrir(parse(fecha));
    }

    private static LocalDate parse(String fecha) {
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La fecha debe tener el formato AAAA-MM-DD.");
        }
    }
}
