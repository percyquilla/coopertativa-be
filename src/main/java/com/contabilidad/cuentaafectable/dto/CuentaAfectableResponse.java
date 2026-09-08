package com.contabilidad.cuentaafectable.dto;

import com.contabilidad.cuentaafectable.entity.CuentaAfectable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CuentaAfectableResponse(
        Long id,
        Long cuentaId,
        String cuentaCodigo,
        String cuentaNombre,
        String codigo,
        Long monedaId,
        String monedaCodigo,
        Long sucursalId,
        Long centroId,
        Long productoId,
        String descripcion,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime fechaHora) {

    public static CuentaAfectableResponse from(CuentaAfectable a,
                                               String cuentaCodigo,
                                               String cuentaNombre,
                                               String monedaCodigo) {
        return new CuentaAfectableResponse(
                a.getId(),
                a.getCuentaId(),
                cuentaCodigo,
                cuentaNombre,
                a.getCodigo(),
                a.getMonedaId(),
                monedaCodigo,
                a.getSucursalId(),
                a.getCentroId(),
                a.getProductoId(),
                a.getDescripcion(),
                a.getEstado(),
                a.getFechaInicio(),
                a.getFechaFin(),
                a.getFechaHora());
    }
}
