package com.bien.parametros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InstitucionRequest(
        @NotBlank @Size(max = 200) String razonSocial,
        @Size(max = 200) String nombreComercial,
        @Size(max = 20) String nit,
        @Size(max = 50) String matricula,
        @Size(max = 20) String codigoRegulador,
        @Size(max = 250) String direccion,
        @Size(max = 30) String telefono,
        @Email @Size(max = 120) String correo,
        Long monedaBaseId,
        @Size(max = 150) String representanteLegal,
        @PastOrPresent LocalDate fechaFundacion,
        @Size(max = 50) String usuario) {
}
