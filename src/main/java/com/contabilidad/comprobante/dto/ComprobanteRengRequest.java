package com.contabilidad.comprobante.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ComprobanteRengRequest(
        @NotNull Long afectableId,
        @NotNull @Pattern(regexp = "D|H") String domDebeHaber,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal monto,
        @Size(max = 100) String glosa) {
}
