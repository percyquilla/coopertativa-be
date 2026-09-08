package com.bien.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 250) String descripcion,
        @NotBlank
        @Pattern(regexp = "AH|CA",
                message = "El tipo de producto debe ser AH (ahorro) o CA (certificado de aportación).")
        String tipoProducto,
        @NotBlank @Size(max = 10) String moneda,
        @NotNull @DecimalMin(value = "0", message = "El saldo mínimo no puede ser negativo.")
        BigDecimal saldoMinimo,
        boolean permiteDeposito,
        boolean permiteRetiro,
        boolean permiteInteres,
        @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (suspendido).")
        String estado,
        @NotNull LocalDate fechaInicio,
        LocalDate fechaFin) {
}
