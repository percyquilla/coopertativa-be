package com.bien.persona.dto;

import com.bien.persona.entity.Persona;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PersonaResponse(
        Long id,
        String apellidoPaterno,
        String apellidoMaterno,
        String apellidoCasada,
        String nombres,
        String cedulaIdentidad,
        String expedicion,
        String genero,
        String lugarTrabajo,
        String cargoActual,
        String correoInstitucional,
        String correoPersonal,
        String numeroTelefono,
        LocalDate fechaNacimiento,
        String estadoCivil,
        String usuario,
        LocalDateTime fechaHora) {

    public static PersonaResponse from(Persona persona) {
        return new PersonaResponse(
                persona.getId(),
                persona.getApellidoPaterno(),
                persona.getApellidoMaterno(),
                persona.getApellidoCasada(),
                persona.getNombres(),
                persona.getCedulaIdentidad(),
                persona.getExpedicion(),
                persona.getGenero(),
                persona.getLugarTrabajo(),
                persona.getCargoActual(),
                persona.getCorreoInstitucional(),
                persona.getCorreoPersonal(),
                persona.getNumeroTelefono(),
                persona.getFechaNacimiento(),
                persona.getEstadoCivil(),
                persona.getUsuario(),
                persona.getFechaHora());
    }
}
