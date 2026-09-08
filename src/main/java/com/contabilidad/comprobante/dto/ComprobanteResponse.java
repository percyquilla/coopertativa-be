package com.contabilidad.comprobante.dto;

import com.contabilidad.comprobante.entity.Comprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @param referencia identificación humana del asiento ("ING-3"); vacía mientras
 *                   el comprobante es borrador, porque el número recién se
 *                   asigna al autorizar.
 * @param cuadra     debe = haber. Un borrador puede estar descuadrado a
 *                   propósito mientras se lo termina de cargar.
 */
public record ComprobanteResponse(
        Long id,
        Long periodoId,
        Long tipoComprobanteId,
        String tipoComprobanteCodigo,
        String tipoComprobanteNombre,
        Long numero,
        String referencia,
        LocalDateTime fecha,
        String glosa,
        String estado,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        boolean cuadra,
        Long operacionId,
        Long comprobanteReversionId,
        String usuarioCreacion,
        LocalDateTime fechaCreacion,
        String usuarioAutoriz,
        LocalDateTime fechaAutoriz,
        List<ComprobanteRengResponse> renglones) {

    public static ComprobanteResponse from(Comprobante c, String tipoCodigo, String tipoNombre,
                                           BigDecimal totalDebe, BigDecimal totalHaber,
                                           List<ComprobanteRengResponse> renglones) {
        return new ComprobanteResponse(
                c.getId(),
                c.getPeriodoId(),
                c.getTipoComprobanteId(),
                tipoCodigo,
                tipoNombre,
                c.getNumero(),
                c.getNumero() == null ? "" : tipoCodigo + "-" + c.getNumero(),
                c.getFecha(),
                c.getGlosa(),
                c.getEstado(),
                totalDebe,
                totalHaber,
                totalDebe.compareTo(totalHaber) == 0,
                c.getOperacionId(),
                c.getComprobanteReversionId(),
                c.getUsuarioCreacion(),
                c.getFechaCreacion(),
                c.getUsuarioAutoriz(),
                c.getFechaAutoriz(),
                renglones);
    }
}
