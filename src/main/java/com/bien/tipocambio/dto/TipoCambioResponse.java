package com.bien.tipocambio.dto;

import com.bien.tipocambio.entity.TipoCambio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TipoCambioResponse(
        Long id,
        Long monedaId,
        Long monedaDestinoId,
        LocalDate fecha,
        BigDecimal compra,
        BigDecimal venta,
        BigDecimal oficial,
        String estado,
        LocalDateTime fechaHora) {

    public static TipoCambioResponse from(TipoCambio t) {
        return new TipoCambioResponse(
                t.getId(),
                t.getMonedaId(),
                t.getMonedaDestinoId(),
                t.getFecha(),
                t.getCompra(),
                t.getVenta(),
                t.getOficial(),
                t.getEstado(),
                t.getFechaHora());
    }
}
