package com.bien.usuario.rest;

import com.bien.usuario.dto.RolResponse;
import com.bien.usuario.repository.RolRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN"})
public class RolResource {

    @Inject
    RolRepository rolRepository;

    @GET
    public List<RolResponse> listAll() {
        return rolRepository.listAll().stream()
                .map(RolResponse::from)
                .toList();
    }
}
