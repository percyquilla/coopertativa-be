package com.contabilidad.tipooperacion.dto;

import com.contabilidad.tipooperacion.entity.TipoOperacion;

import java.time.LocalDate;

public record TipoOperacionResponse(
        Long id,
        String codigo,
        String descripcion,
        String estado,
        boolean vigente,
        Long esquemaContableId,
        String esquemaCodigo,
        Integer esquemaVersion,
        Long tipoComprobanteId,
        String tipoComprobanteCodigo) {

    public static TipoOperacionResponse from(TipoOperacion t, String esquemaCodigo,
                                             Integer esquemaVersion, String tipoComprobanteCodigo) {
        return new TipoOperacionResponse(
                t.getId(), t.getCodigo(), t.getDescripcion(), t.getEstado(),
                t.estaVigente(LocalDate.now()), t.getEsquemaContableId(), esquemaCodigo, esquemaVersion,
                t.getTipoComprobanteId(), tipoComprobanteCodigo);
    }
}
