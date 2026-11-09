package com.bien.certificadoaportacion.dto;

import com.bien.certificadoaportacion.entity.CertificadoAportacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CertificadoAportacionResponse(
        Long id,
        Long socioId,
        String numero,
        BigDecimal valorUnitario,
        Integer cantidad,
        BigDecimal montoNominal,
        LocalDate fechaEmision,
        String estadoVerif,
        String estado,
        Long operacionId,
        String usuario,
        LocalDateTime fechaHora,
        String estacion) {

    public static CertificadoAportacionResponse from(CertificadoAportacion c) {
        return new CertificadoAportacionResponse(
                c.getId(),
                c.getSocioId(),
                c.getNumero(),
                c.getValorUnitario(),
                c.getCantidad(),
                c.getValorUnitario().multiply(BigDecimal.valueOf(c.getCantidad())),
                c.getFechaEmision(),
                c.getEstadoVerif(),
                c.getEstado(),
                c.getOperacionId(),
                c.getUsuario(),
                c.getFechaHora(),
                c.getEstacion());
    }
}
