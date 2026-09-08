package com.bien.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioRequest(
        @NotNull Long personaId,
        Long sucursalId,
        @NotBlank @Size(max = 50) String username,
        @Email @Size(max = 120) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotEmpty Set<@NotBlank String> roles) {
}
