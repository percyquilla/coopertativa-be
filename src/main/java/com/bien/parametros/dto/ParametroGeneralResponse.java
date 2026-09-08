package com.bien.parametros.dto;

import com.bien.parametros.entity.ParametroGeneral;

import java.time.LocalDateTime;

public record ParametroGeneralResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String valor,
        String tipoDato,
        boolean editable,
        String estado,
        LocalDateTime fechaHora) {

    public static ParametroGeneralResponse from(ParametroGeneral p) {
        return new ParametroGeneralResponse(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getValor(),
                p.getTipoDato(),
                p.isEditable(),
                p.getEstado(),
                p.getFechaHora());
    }
}
