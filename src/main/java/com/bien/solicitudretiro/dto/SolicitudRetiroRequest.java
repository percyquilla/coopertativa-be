package com.bien.solicitudretiro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudRetiroRequest(
        @NotNull Long socioId,
        @Size(max = 300) String motivo,
        @Size(max = 50) String usuario) {
}
