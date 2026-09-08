package com.contabilidad.clasificacioncuenta.rest;

import com.contabilidad.clasificacioncuenta.dto.ClasificacionCuentaResponse;
import com.contabilidad.clasificacioncuenta.service.ClasificacionCuentaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/clasificaciones-cuenta")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class ClasificacionCuentaResource {

    @Inject
    ClasificacionCuentaService service;

    @GET
    public List<ClasificacionCuentaResponse> listAll() {
        return service.listAll();
    }
}
