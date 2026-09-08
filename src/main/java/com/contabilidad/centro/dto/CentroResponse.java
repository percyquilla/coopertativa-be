package com.contabilidad.centro.dto;

import com.contabilidad.centro.entity.Centro;

public record CentroResponse(
        Long id,
        String codigo,
        String nombre,
        String estado) {

    public static CentroResponse from(Centro c) {
        return new CentroResponse(c.getId(), c.getCodigo(), c.getNombre(), c.getEstado());
    }
}
