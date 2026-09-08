package com.bien.moneda.dto;

import com.bien.moneda.entity.Moneda;

public record MonedaResponse(
        Long id,
        String codigo,
        String descripcion,
        String simbolo,
        String estado) {

    public static MonedaResponse from(Moneda m) {
        return new MonedaResponse(m.getId(), m.getCodigo(), m.getDescripcion(), m.getSimbolo(), m.getEstado());
    }
}
