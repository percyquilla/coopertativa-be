package com.contabilidad.tipooperacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TipoOperacionRequest(
        @NotBlank @Size(max = 20) String codigo,
        @Size(max = 250) String descripcion,
        @NotNull Long esquemaContableId,
        @NotNull Long tipoComprobanteId,
        @NotNull @Pattern(regexp = "V|S") String estado) {
}
