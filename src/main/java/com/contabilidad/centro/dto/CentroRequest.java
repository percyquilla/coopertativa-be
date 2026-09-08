package com.contabilidad.centro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CentroRequest(
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (suspendido).")
        String estado) {
}
