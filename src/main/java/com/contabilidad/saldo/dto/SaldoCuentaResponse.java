package com.contabilidad.saldo.dto;

import java.math.BigDecimal;

/**
 * Una fila del balance de comprobación: el saldo de una cuenta afectable en un
 * período, con las columnas ya separadas en deudor / acreedor según dónde quedó
 * el {@code saldoFinal}.
 */
public record SaldoCuentaResponse(
        Long afectableId,
        String afectableCodigo,
        String cuentaNombre,
        String naturaleza,
        BigDecimal saldoInicial,
        BigDecimal totalDebito,
        BigDecimal totalCredito,
        BigDecimal saldoFinal,
        BigDecimal saldoDeudor,
        BigDecimal saldoAcreedor) {
}
