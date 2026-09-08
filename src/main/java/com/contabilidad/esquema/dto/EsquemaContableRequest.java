package com.contabilidad.esquema.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Alta o edición de un esquema contable con su detalle completo. El detalle se
 * reemplaza entero en cada guardado; la renumeración de secuencias la hace el
 * servicio.
 */
public record EsquemaContableRequest(
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @Pattern(regexp = "V|S") String estado,
        @NotNull @Size(min = 2, message = "Un esquema necesita al menos un débito y un crédito")
        @Valid List<EsquemaDetalleRequest> detalle) {
}
