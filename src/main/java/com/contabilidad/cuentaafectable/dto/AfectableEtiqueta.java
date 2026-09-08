package com.contabilidad.cuentaafectable.dto;

/**
 * Lo mínimo para mostrar una cuenta afectable en un asiento o un reporte, sin
 * arrastrar la entidad entera. Se resuelve por lote (ver
 * {@code CuentaAfectableService.etiquetas}) para no consultar una vez por renglón.
 *
 * @param naturaleza D o H, heredada de la clasificación de la cuenta de mayor:
 *                   define de qué lado suma el saldo.
 */
public record AfectableEtiqueta(
        Long id,
        String codigo,
        String cuentaCodigo,
        String cuentaNombre,
        String naturaleza) {
}
