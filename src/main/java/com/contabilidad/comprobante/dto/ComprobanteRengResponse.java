package com.contabilidad.comprobante.dto;

import com.contabilidad.comprobante.entity.ComprobanteReng;

import java.math.BigDecimal;

public record ComprobanteRengResponse(
        Long id,
        Integer secuencia,
        Long afectableId,
        String afectableCodigo,
        String cuentaNombre,
        String domDebeHaber,
        BigDecimal monto,
        String glosa) {

    public static ComprobanteRengResponse from(ComprobanteReng r, String afectableCodigo, String cuentaNombre) {
        return new ComprobanteRengResponse(
                r.getId(),
                r.getSecuencia(),
                r.getAfectableId(),
                afectableCodigo,
                cuentaNombre,
                r.getDomDebeHaber(),
                r.getMonto(),
                r.getGlosa());
    }
}
