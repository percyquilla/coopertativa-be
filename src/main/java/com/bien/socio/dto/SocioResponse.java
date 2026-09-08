package com.bien.socio.dto;

import com.bien.socio.entity.Socio;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SocioResponse(
        Long id,
        Long personaId,
        String numeroSocio,
        Long sucursalId,
        LocalDate fechaIngreso,
        LocalDate fechaRetiro,
        String estado,
        String usuario,
        LocalDateTime fechaHora) {

    public static SocioResponse from(Socio socio) {
        return new SocioResponse(
                socio.getId(),
                socio.getPersonaId(),
                socio.getNumeroSocio(),
                socio.getSucursalId(),
                socio.getFechaIngreso(),
                socio.getFechaRetiro(),
                socio.getEstado(),
                socio.getUsuario(),
                socio.getFechaHora());
    }
}
