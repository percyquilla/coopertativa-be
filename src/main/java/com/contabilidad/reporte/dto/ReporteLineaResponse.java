package com.contabilidad.reporte.dto;

import java.util.List;

/**
 * Una línea de la estructura de un reporte, con sus cuentas mapeadas resueltas
 * (código y nombre) para la pantalla de ABM.
 */
public record ReporteLineaResponse(
        Long id,
        Long estructuraReporteId,
        Long lineaPadreId,
        String codigo,
        String titulo,
        Integer orden,
        String tipoLinea,
        Short signo,
        String formula,
        String estado,
        List<Cuenta> cuentas) {

    public record Cuenta(Long id, Long cuentaId, String cuentaCodigo, String cuentaNombre, Short signo) {
    }
}
