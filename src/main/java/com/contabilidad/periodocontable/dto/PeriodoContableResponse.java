package com.contabilidad.periodocontable.dto;

import com.contabilidad.periodocontable.entity.PeriodoContable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoContableResponse(
        Long id,
        Integer gestion,
        Short mes,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado,
        boolean abierto,
        String usuario,
        LocalDateTime fechaHora) {

    public static PeriodoContableResponse from(PeriodoContable p) {
        return new PeriodoContableResponse(
                p.getId(),
                p.getGestion(),
                p.getMes(),
                p.getFechaInicio(),
                p.getFechaFin(),
                p.getEstado(),
                !"C".equals(p.getEstado()),
                p.getUsuario(),
                p.getFechaHora());
    }
}
