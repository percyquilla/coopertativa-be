package com.bien.parametros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FeriadoRequest(
        @NotNull LocalDate fecha,
        @NotBlank @Size(max = 150) String descripcion,
        @NotBlank @Pattern(regexp = "NAC|DEP|LOC", message = "ambito debe ser NAC, DEP o LOC") String ambito,
        Long sucursalId,
        Boolean recurrente,
        @Size(max = 50) String usuario) {
}
