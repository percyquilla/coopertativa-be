package com.contabilidad.comprobante.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Alta o edición de un comprobante manual. El período no viaja: se deduce de la
 * fecha, que es la única que el contador realmente elige.
 */
public record ComprobanteRequest(
        @NotNull Long tipoComprobanteId,
        @NotNull LocalDate fecha,
        @NotBlank @Size(max = 300) String glosa,
        @NotNull @Size(min = 2, message = "Un asiento necesita al menos dos renglones")
        @Valid List<ComprobanteRengRequest> renglones) {
}
