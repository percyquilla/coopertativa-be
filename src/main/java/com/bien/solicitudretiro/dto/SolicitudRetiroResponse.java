package com.bien.solicitudretiro.dto;

import com.bien.solicitudretiro.entity.SolicitudRetiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudRetiroResponse(
        Long id,
        Long socioId,
        LocalDate fechaSolicitud,
        String motivo,
        String estado,
        BigDecimal montoAportes,
        LocalDate fechaResolucion,
        String usuarioResolucion,
        String observacionResolucion,
        LocalDate fechaDevolucion,
        String usuario,
        LocalDateTime fechaHora) {

    public static SolicitudRetiroResponse from(SolicitudRetiro s) {
        return new SolicitudRetiroResponse(
                s.getId(),
                s.getSocioId(),
                s.getFechaSolicitud(),
                s.getMotivo(),
                s.getEstado(),
                s.getMontoAportes(),
                s.getFechaResolucion(),
                s.getUsuarioResolucion(),
                s.getObservacionResolucion(),
                s.getFechaDevolucion(),
                s.getUsuario(),
                s.getFechaHora());
    }
}
