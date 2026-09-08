package com.contabilidad.tipocomprobante.dto;

import com.contabilidad.tipocomprobante.entity.TipoComprobante;

public record TipoComprobanteResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String prefijo,
        String estado,
        boolean vigente) {

    public static TipoComprobanteResponse from(TipoComprobante t) {
        return new TipoComprobanteResponse(
                t.getId(),
                t.getCodigo(),
                t.getNombre(),
                t.getDescripcion(),
                t.getPrefijo(),
                t.getEstado(),
                "V".equals(t.getEstado()));
    }
}
