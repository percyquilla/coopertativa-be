package com.bien.cuentatitular.dto;

import com.bien.cuentatitular.entity.CuentaTitular;

import java.time.LocalDate;

public record CuentaTitularResponse(
        Long id,
        Long cuentaCaptacionId,
        Long personaId,
        String tipoTitular,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado) {

    public static CuentaTitularResponse from(CuentaTitular t) {
        return new CuentaTitularResponse(
                t.getId(),
                t.getCuentaCaptacionId(),
                t.getPersonaId(),
                t.getTipoTitular(),
                t.getFechaInicio(),
                t.getFechaFin(),
                t.getEstado());
    }
}
