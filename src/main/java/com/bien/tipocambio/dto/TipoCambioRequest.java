package com.bien.tipocambio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TipoCambioRequest(
        @NotNull Long monedaId,
        @NotNull Long monedaDestinoId,
        @NotNull LocalDate fecha,
        @NotNull @Positive BigDecimal compra,
        @NotNull @Positive BigDecimal venta,
        @Positive BigDecimal oficial,
        @Size(max = 50) String usuario) {
}
