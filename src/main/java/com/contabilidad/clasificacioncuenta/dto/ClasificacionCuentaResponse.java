package com.contabilidad.clasificacioncuenta.dto;

import com.contabilidad.clasificacioncuenta.entity.ClasificacionCuenta;

public record ClasificacionCuentaResponse(
        Long id,
        String codigo,
        String naturaleza,
        String descripcion,
        String estado) {

    public static ClasificacionCuentaResponse from(ClasificacionCuenta c) {
        return new ClasificacionCuentaResponse(
                c.getId(),
                c.getCodigo(),
                c.getNaturaleza(),
                c.getDescripcion(),
                c.getEstado());
    }
}
