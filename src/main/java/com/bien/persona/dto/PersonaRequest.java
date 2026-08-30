package com.bien.persona.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PersonaRequest(
        @NotBlank @Size(max = 50) String apellidoPaterno,
        @NotBlank @Size(max = 50) String apellidoMaterno,
        @Size(max = 50) String apellidoCasada,
        @NotBlank @Size(max = 60) String nombres,
        @NotBlank @Size(max = 20) String cedulaIdentidad,
        @Size(max = 5) String expedicion,
        @NotBlank @Size(min = 1, max = 1) String genero,
        @Size(max = 80) String lugarTrabajo,
        @Size(max = 150) String cargoActual,
        @Email @Size(max = 80) String correoInstitucional,
        @Email @Size(max = 80) String correoPersonal,
        @NotBlank @Size(max = 10) String numeroTelefono,
        @NotNull @Past LocalDate fechaNacimiento,
        @NotBlank @Size(max = 20) String estadoCivil,
        @Size(max = 50) String usuario) {
}
