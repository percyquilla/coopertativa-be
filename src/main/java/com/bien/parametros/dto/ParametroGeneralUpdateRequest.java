package com.bien.parametros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParametroGeneralUpdateRequest(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 250) String descripcion,
        @NotBlank @Size(max = 500) String valor,
        Boolean editable,
        @Size(max = 50) String usuario) {
}
