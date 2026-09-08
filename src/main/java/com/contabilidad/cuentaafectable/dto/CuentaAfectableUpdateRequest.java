package com.contabilidad.cuentaafectable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * La cuenta de mayor y las dimensiones (moneda/sucursal/centro/producto) quedan
 * fijas: cambiarlas movería de sitio asientos y saldos ya grabados.
 */
public record CuentaAfectableUpdateRequest(
        @NotBlank @Size(max = 30) String codigo,
        @Size(max = 250) String descripcion,
        @NotBlank @Pattern(regexp = "V|A", message = "estado debe ser V o A") String estado,
        @Size(max = 50) String usuario) {
}
