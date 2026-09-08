package com.bien.sucursal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SucursalRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (suspendida).")
        String estado) {
}
