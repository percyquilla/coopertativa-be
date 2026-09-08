package com.contabilidad.reporte.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Alta/edición de una línea de reporte. Para SUBTOTAL/TOTAL, {@code formula} es
 * obligatoria y se valida su sintaxis; para DETALLE, {@code cuentas} lleva el
 * mapeo cuenta↔signo (para el resto se ignora).
 */
public record ReporteLineaRequest(
        Long lineaPadreId,
        @NotBlank @Size(max = 30)
        @Pattern(regexp = "[A-Za-z_][A-Za-z0-9_]*",
                message = "El código solo puede tener letras, números y guion bajo, y no empezar con número.")
        String codigo,
        @NotBlank @Size(max = 200) String titulo,
        @NotNull Integer orden,
        @NotBlank
        @Pattern(regexp = "TITULO|DETALLE|SUBTOTAL|TOTAL",
                message = "El tipo de línea debe ser TITULO, DETALLE, SUBTOTAL o TOTAL.")
        String tipoLinea,
        @NotNull Short signo,
        @Size(max = 500) String formula,
        @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (suspendido).")
        String estado,
        List<Cuenta> cuentas) {

    public record Cuenta(@NotNull Long cuentaId, @NotNull Short signo) {
    }
}
