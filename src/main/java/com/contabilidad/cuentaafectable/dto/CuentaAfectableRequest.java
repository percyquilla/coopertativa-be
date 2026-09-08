package com.contabilidad.cuentaafectable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CuentaAfectableRequest(
        @NotNull Long cuentaId,
        @NotBlank @Size(max = 30) String codigo,
        Long monedaId,
        Long sucursalId,
        Long centroId,
        Long productoId,
        @Size(max = 250) String descripcion,
        @Size(max = 50) String usuario) {
}
