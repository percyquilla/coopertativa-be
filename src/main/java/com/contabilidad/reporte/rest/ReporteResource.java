package com.contabilidad.reporte.rest;

import com.contabilidad.reporte.dto.ReporteResponse;
import com.contabilidad.reporte.service.ReporteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/reportes")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class ReporteResource {

    @Inject
    ReporteService service;

    @GET
    @Path("/esf")
    public ReporteResponse esf(@QueryParam("periodoId") Long periodoId) {
        return service.esf(periodoId);
    }

    @GET
    @Path("/er")
    public ReporteResponse er(@QueryParam("periodoId") Long periodoId) {
        return service.er(periodoId);
    }
}
