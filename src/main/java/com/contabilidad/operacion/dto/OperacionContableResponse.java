package com.contabilidad.operacion.dto;

import com.contabilidad.operacion.entity.OperacionContable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperacionContableResponse(
        Long id,
        Long tipoOperacionId,
        String tipoOperacionCodigo,
        LocalDateTime fechaOperacion,
        String glosa,
        BigDecimal monto,
        String estado,
        Long esquemaContableId,
        String origenModulo,
        String origenTabla,
        Long origenId,
        Long comprobanteId,
        String errorMensaje,
        Integer intentos,
        LocalDateTime fechaProceso) {

    public static OperacionContableResponse from(OperacionContable o, String tipoOperacionCodigo,
                                                 Long comprobanteId) {
        return new OperacionContableResponse(
                o.getId(), o.getTipoOperacionId(), tipoOperacionCodigo, o.getFechaOperacion(),
                o.getGlosa(), o.getMonto(), o.getEstado(), o.getEsquemaContableId(),
                o.getOrigenModulo(), o.getOrigenTabla(), o.getOrigenId(), comprobanteId,
                o.getErrorMensaje(), o.getIntentos(), o.getFechaProceso());
    }
}
