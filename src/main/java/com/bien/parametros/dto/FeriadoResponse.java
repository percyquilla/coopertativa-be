package com.bien.parametros.dto;

import com.bien.parametros.entity.Feriado;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeriadoResponse(
        Long id,
        LocalDate fecha,
        String descripcion,
        String ambito,
        Long sucursalId,
        boolean recurrente,
        String estado,
        LocalDateTime fechaHora) {

    public static FeriadoResponse from(Feriado f) {
        return new FeriadoResponse(
                f.getId(),
                f.getFecha(),
                f.getDescripcion(),
                f.getAmbito(),
                f.getSucursalId(),
                f.isRecurrente(),
                f.getEstado(),
                f.getFechaHora());
    }
}
