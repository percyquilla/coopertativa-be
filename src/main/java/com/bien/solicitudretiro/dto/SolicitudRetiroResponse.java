package com.bien.solicitudretiro.dto;

import com.bien.solicitudretiro.entity.SolicitudRetiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudRetiroResponse(
        Long id,
        Long socioId,
        Long certificadoId,
        String numeroCertificado,
        LocalDate fechaSolicitud,
        String motivo,
        String estado,
        BigDecimal montoAportes,
        LocalDate fechaResolucion,
        String usuarioResolucion,
        String observacionResolucion,
        LocalDate fechaDevolucion,
        Long operacionId,
        String usuario,
        LocalDateTime fechaHora,
        String estacion) {

    public static SolicitudRetiroResponse from(SolicitudRetiro s, String numeroCertificado) {
        return new SolicitudRetiroResponse(
                s.getId(),
                s.getSocioId(),
                s.getCertificadoId(),
                numeroCertificado,
                s.getFechaSolicitud(),
                s.getMotivo(),
                s.getEstado(),
                s.getMontoAportes(),
                s.getFechaResolucion(),
                s.getUsuarioResolucion(),
                s.getObservacionResolucion(),
                s.getFechaDevolucion(),
                s.getOperacionId(),
                s.getUsuario(),
                s.getFechaHora(),
                s.getEstacion());
    }
}
