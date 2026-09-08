package com.bien.sucursal.dto;

import com.bien.sucursal.entity.Sucursal;

public record SucursalResponse(
        Long id,
        String codigo,
        String nombre,
        String estado) {

    public static SucursalResponse from(Sucursal sucursal) {
        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getCodigo(),
                sucursal.getNombre(),
                sucursal.getEstado());
    }
}
