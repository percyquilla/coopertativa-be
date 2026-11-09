package com.bien.socio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila del reporte de socios (Obs. #5). {@code montoAportaciones} es la suma
 * del valor nominal de los certificados vigentes (VIG) del socio.
 */
public record SocioReporteRow(
        Long socioId,
        String numeroSocio,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String apellidoCasada,
        String cedulaIdentidad,
        String expedicion,
        String telefono,
        String correo,
        String direccion,
        String genero,
        LocalDate fechaIngreso,
        BigDecimal montoAportaciones,
        String estado) {
}
