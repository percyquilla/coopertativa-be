package com.bien.movimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MovimientoRequest(
        @NotBlank @Pattern(regexp = "DEP|RET", message = "tipoMovimiento debe ser DEP o RET") String tipoMovimiento,
        @NotNull @Positive BigDecimal monto,
        @Size(max = 100) String referencia,
        @Size(max = 250) String descripcion,
        @Size(max = 50) String usuario) {
}
