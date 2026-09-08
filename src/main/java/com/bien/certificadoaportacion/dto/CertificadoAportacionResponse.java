package com.bien.certificadoaportacion.dto;

import com.bien.certificadoaportacion.entity.CertificadoAportacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CertificadoAportacionResponse(
        Long id,
        Long socioId,
        Long cuentaCaptacionId,
        String numeroCuenta,
        String numero,
        BigDecimal valorUnitario,
        Integer cantidad,
        BigDecimal montoNominal,
        BigDecimal saldoCuenta,
        LocalDate fechaEmision,
        String estadoVerif,
        String estado,
        String usuario,
        LocalDateTime fechaHora) {

    public static CertificadoAportacionResponse from(CertificadoAportacion c,
                                                     String numeroCuenta,
                                                     BigDecimal saldoCuenta) {
        return new CertificadoAportacionResponse(
                c.getId(),
                c.getSocioId(),
                c.getCuentaCaptacionId(),
                numeroCuenta,
                c.getNumero(),
                c.getValorUnitario(),
                c.getCantidad(),
                c.getValorUnitario().multiply(BigDecimal.valueOf(c.getCantidad())),
                saldoCuenta,
                c.getFechaEmision(),
                c.getEstadoVerif(),
                c.getEstado(),
                c.getUsuario(),
                c.getFechaHora());
    }
}
