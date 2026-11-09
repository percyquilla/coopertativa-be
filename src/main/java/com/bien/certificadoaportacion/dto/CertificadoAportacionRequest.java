package com.bien.certificadoaportacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Registro de emisión. El número de certificado lo asigna el backend
 * (Obs. #7); si {@code valorUnitario} no viene, se toma el parámetro
 * {@code VALOR_UNIT_APORTE} (Obs. #8).
 */
public record CertificadoAportacionRequest(
        @NotNull Long socioId,
        @Positive BigDecimal valorUnitario,
        @NotNull @Positive Integer cantidad,
        @Size(max = 50) String usuario) {
}
