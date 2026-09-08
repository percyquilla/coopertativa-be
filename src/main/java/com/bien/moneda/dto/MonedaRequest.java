package com.bien.moneda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MonedaRequest(
        @NotBlank @Size(max = 10) String codigo,
        @NotBlank @Size(max = 100) String descripcion,
        @Size(max = 10) String simbolo,
        @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (suspendida).") String estado) {
}
