package com.contabilidad.reporte.dto;

/** Cabecera de un reporte configurable, para el selector del ABM. */
public record EstructuraReporteResumen(Long id, String codigo, String nombre, String tipo, String estado) {
}
