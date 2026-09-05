package com.bien.usuario.rest;

import com.bien.usuario.dto.LoginRequest;
import com.bien.usuario.dto.LoginResponse;
import com.bien.usuario.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }
}
