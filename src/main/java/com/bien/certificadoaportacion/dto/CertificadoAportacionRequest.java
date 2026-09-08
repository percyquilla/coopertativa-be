package com.bien.certificadoaportacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CertificadoAportacionRequest(
        @NotNull Long socioId,
        @NotBlank @Size(max = 30) String numero,
        @NotNull @Positive BigDecimal valorUnitario,
        @NotNull @Positive Integer cantidad,
        @Size(max = 50) String usuario) {
}
