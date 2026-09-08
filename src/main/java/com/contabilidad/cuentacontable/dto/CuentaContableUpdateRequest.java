package com.contabilidad.cuentacontable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solo lo no estructural: código, padre, nivel y tipo quedan fijos una vez
 * creada la cuenta, porque cambiarlos rompería la jerarquía y los asientos ya
 * grabados contra sus cuentas afectables.
 */
public record CuentaContableUpdateRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 500) String descripcion,
        @NotBlank @Pattern(regexp = "V|A", message = "estado debe ser V o A") String estado,
        @Size(max = 50) String usuario) {
}
