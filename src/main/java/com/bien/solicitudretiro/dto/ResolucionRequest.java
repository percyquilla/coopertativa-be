package com.bien.solicitudretiro.dto;

import jakarta.validation.constraints.Size;

/** Datos de la resolución (aprobar / rechazar / anular / pagar). */
public record ResolucionRequest(
        @Size(max = 300) String observacion,
        @Size(max = 50) String usuario) {
}
