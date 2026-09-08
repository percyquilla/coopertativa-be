package com.bien.cuentacaptacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CuentaCaptacionRequest(
        @NotBlank @Size(max = 30) String numeroCuenta,
        @NotNull Long productoId,
        @NotBlank @Pattern(regexp = "IND|MNC", message = "tipoCuenta debe ser IND o MNC") String tipoCuenta,
        @Size(max = 50) String usuario) {
}
