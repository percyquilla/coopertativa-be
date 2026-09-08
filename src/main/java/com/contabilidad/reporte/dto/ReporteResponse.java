package com.contabilidad.reporte.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param descuadre para el ESF, activo − (pasivo + patrimonio); debería ser 0.
 *                  Null para el ER.
 */
public record ReporteResponse(
        String codigo,
        String nombre,
        String tipo,
        Long periodoId,
        Integer gestion,
        Short mes,
        List<Linea> lineas,
        BigDecimal descuadre) {

    public record Linea(
            String codigo,
            String titulo,
            String tipoLinea,
            int nivel,
            BigDecimal monto) {
    }
}
