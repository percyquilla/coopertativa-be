package com.contabilidad.saldo.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Balance de comprobación de un período. Dos igualdades tienen que cumplirse:
 * la suma de débitos iguala a la de créditos (todo comprobante cuadra), y la
 * suma de saldos deudores iguala a la de acreedores. {@code cuadra} es true solo
 * si las dos se cumplen.
 */
public record BalanceComprobacionResponse(
        Long periodoId,
        Integer gestion,
        Short mes,
        List<SaldoCuentaResponse> filas,
        BigDecimal sumaDebitos,
        BigDecimal sumaCreditos,
        BigDecimal sumaSaldosDeudores,
        BigDecimal sumaSaldosAcreedores,
        boolean cuadra) {
}
