package com.bien.movimiento.dto;

import com.bien.movimiento.entity.Movimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        Long cuentaCaptacionId,
        String tipoMovimiento,
        LocalDateTime fechaHora,
        BigDecimal monto,
        BigDecimal saldoAnterior,
        BigDecimal saldoPosterior,
        String referencia,
        String descripcion,
        String estadoMov) {

    public static MovimientoResponse from(Movimiento m) {
        return new MovimientoResponse(
                m.getId(),
                m.getCuentaCaptacionId(),
                m.getTipoMovimiento(),
                m.getFechaHora(),
                m.getMonto(),
                m.getSaldoAnterior(),
                m.getSaldoPosterior(),
                m.getReferencia(),
                m.getDescripcion(),
                m.getEstadoMov());
    }
}
