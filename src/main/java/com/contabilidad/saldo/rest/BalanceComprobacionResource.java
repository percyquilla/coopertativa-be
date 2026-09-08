package com.contabilidad.saldo.rest;

import com.contabilidad.saldo.dto.BalanceComprobacionResponse;
import com.contabilidad.saldo.service.SaldoCuentaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/balance-comprobacion")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class BalanceComprobacionResource {

    @Inject
    SaldoCuentaService service;

    @GET
    public BalanceComprobacionResponse balance(@QueryParam("periodoId") Long periodoId) {
        return service.balanceComprobacion(periodoId);
    }
}
