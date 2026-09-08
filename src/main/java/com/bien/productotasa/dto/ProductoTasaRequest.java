package com.bien.productotasa.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoTasaRequest(
        @NotNull
        @DecimalMin(value = "0", message = "La tasa no puede ser negativa.")
        @DecimalMax(value = "100", message = "La tasa se expresa en porcentaje: no puede pasar de 100.")
        BigDecimal tasa,
        @NotBlank
        @Pattern(regexp = "E|N", message = "El tipo de tasa debe ser E (efectiva) o N (nominal).")
        String tipoTasa,
        @NotNull
        @Pattern(regexp = "360|365", message = "La base de días debe ser 360 o 365.")
        String baseDias,
        @NotNull LocalDate fechaInicio,
        LocalDate fechaFin,
        @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (suspendida).")
        String estado) {
}
