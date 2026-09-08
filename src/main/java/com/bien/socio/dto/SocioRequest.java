package com.bien.socio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SocioRequest(
        @NotNull Long personaId,
        @NotBlank @Size(max = 20) String numeroSocio,
        @NotNull Long sucursalId,
        @NotNull @PastOrPresent LocalDate fechaIngreso,
        @Size(max = 50) String usuario) {
}
