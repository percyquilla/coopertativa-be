package com.contabilidad.esquema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EsquemaDetalleRequest(
        @NotNull Long afectableId,
        @NotNull @Pattern(regexp = "D|H") String domDebeHaber,
        @NotBlank @Size(max = 500) String formula,
        @Size(max = 250) String descripcion) {
}
