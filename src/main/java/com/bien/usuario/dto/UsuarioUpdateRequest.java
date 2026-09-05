package com.bien.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @NotNull Long personaId,
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Pattern(regexp = "ADMIN|USER") String rol) {
}
