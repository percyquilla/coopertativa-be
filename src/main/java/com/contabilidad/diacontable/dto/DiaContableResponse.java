package com.contabilidad.diacontable.dto;

import java.time.LocalDate;

/**
 * Estado de una fecha para la pantalla de cierre diario.
 *
 * @param estado            'P' pendiente · 'A' abierto · 'C' cerrado · 'R' reabierto · 'SIN_ABRIR' si no hay fila
 * @param operacionesPendientes asientos automáticos que fallaron ese día
 * @param comprobantesBorrador  asientos manuales sin autorizar ese día
 * @param puedeCerrar       true si no queda nada pendiente y el día está abierto o reabierto
 * @param interesesDevengados cuántas cuentas devengaron intereses ese día (Fase 6)
 */
public record DiaContableResponse(
        LocalDate fecha,
        String estado,
        Long periodoId,
        Integer periodoGestion,
        Short periodoMes,
        boolean periodoCerrado,
        long operacionesPendientes,
        long comprobantesBorrador,
        boolean puedeCerrar,
        long interesesDevengados) {
}
