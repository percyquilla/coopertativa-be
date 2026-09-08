package com.contabilidad.saldo.rest;

import com.contabilidad.saldo.dto.MayorResponse;
import com.contabilidad.saldo.service.SaldoCuentaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/mayor")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class MayorResource {

    @Inject
    SaldoCuentaService service;

    @GET
    public MayorResponse mayor(@QueryParam("afectableId") Long afectableId,
                               @QueryParam("periodoId") Long periodoId) {
        return service.mayor(afectableId, periodoId);
    }
}
