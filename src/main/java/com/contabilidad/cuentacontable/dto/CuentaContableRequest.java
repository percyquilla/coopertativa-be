package com.contabilidad.cuentacontable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CuentaContableRequest(
        Long cuentaPadreId,
        @NotNull Long clasificacionId,
        @NotBlank @Pattern(regexp = "AGR|MAY", message = "tipoCuenta debe ser AGR o MAY") String tipoCuenta,
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 500) String descripcion,
        @Size(max = 50) String usuario) {
}
