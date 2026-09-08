package com.bien.cuentatitular.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CuentaTitularRequest(
        @NotNull Long personaId,
        @NotBlank @Pattern(regexp = "P|S", message = "tipoTitular debe ser P (principal) o S (cotitular)") String tipoTitular,
        @Size(max = 50) String usuario) {
}
