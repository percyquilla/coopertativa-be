package com.bien.solicitudretiro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Registro de la solicitud: el socio retira un único certificado vigente (Obs. #11). */
public record SolicitudRetiroRequest(
        @NotNull Long socioId,
        @NotNull Long certificadoId,
        @Size(max = 300) String motivo,
        @Size(max = 50) String usuario) {
}
