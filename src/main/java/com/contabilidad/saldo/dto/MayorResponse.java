package com.contabilidad.saldo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mayor de una cuenta afectable en un período: el saldo con el que arranca, los
 * movimientos de los comprobantes autorizados con su saldo corrido, y el saldo
 * con el que cierra.
 */
public record MayorResponse(
        Long afectableId,
        String afectableCodigo,
        String cuentaNombre,
        String naturaleza,
        Long periodoId,
        BigDecimal saldoInicial,
        BigDecimal saldoFinal,
        List<Linea> lineas) {

    public record Linea(
            Long comprobanteId,
            String referencia,
            LocalDateTime fecha,
            String glosa,
            BigDecimal debe,
            BigDecimal haber,
            BigDecimal saldo) {
    }
}
