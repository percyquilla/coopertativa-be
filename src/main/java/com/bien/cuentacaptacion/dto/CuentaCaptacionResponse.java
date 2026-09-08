package com.bien.cuentacaptacion.dto;

import com.bien.cuentacaptacion.entity.CuentaCaptacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CuentaCaptacionResponse(
        Long id,
        String numeroCuenta,
        Long productoId,
        String tipoCuenta,
        String moneda,
        LocalDate fechaApertura,
        LocalDate fechaCierre,
        BigDecimal saldoActual,
        BigDecimal saldoBloqueado,
        BigDecimal saldoDisponible,
        String estadoCuenta,
        LocalDateTime fechaHora) {

    public static CuentaCaptacionResponse from(CuentaCaptacion c) {
        return new CuentaCaptacionResponse(
                c.getId(),
                c.getNumeroCuenta(),
                c.getProductoId(),
                c.getTipoCuenta(),
                c.getMoneda(),
                c.getFechaApertura(),
                c.getFechaCierre(),
                c.getSaldoActual(),
                c.getSaldoBloqueado(),
                c.getSaldoActual().subtract(c.getSaldoBloqueado()),
                c.getEstadoCuenta(),
                c.getFechaHora());
    }
}
