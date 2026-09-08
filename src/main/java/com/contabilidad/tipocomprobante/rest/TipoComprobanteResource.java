package com.contabilidad.tipocomprobante.rest;

import com.contabilidad.tipocomprobante.dto.TipoComprobanteResponse;
import com.contabilidad.tipocomprobante.service.TipoComprobanteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/tipos-comprobante")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "CONTADOR", "GERENTE", "AUDITOR"})
public class TipoComprobanteResource {

    @Inject
    TipoComprobanteService service;

    @GET
    public List<TipoComprobanteResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public TipoComprobanteResponse findById(@PathParam("id") Long id) {
        return service.findById(id);
    }
}
