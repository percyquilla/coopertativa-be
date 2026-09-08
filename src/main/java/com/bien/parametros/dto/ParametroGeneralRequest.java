package com.bien.parametros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ParametroGeneralRequest(
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 250) String descripcion,
        @NotBlank @Size(max = 500) String valor,
        @NotBlank @Pattern(regexp = "TXT|NUM|DEC|DAT|DOM", message = "tipoDato debe ser TXT, NUM, DEC, DAT o DOM") String tipoDato,
        Boolean editable,
        @Size(max = 50) String usuario) {
}
