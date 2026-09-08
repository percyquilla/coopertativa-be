package com.bien.usuario.dto;

import com.bien.usuario.entity.Rol;

public record RolResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        boolean esSistema) {

    public static RolResponse from(Rol rol) {
        return new RolResponse(
                rol.getId(),
                rol.getCodigo(),
                rol.getNombre(),
                rol.getDescripcion(),
                rol.isEsSistema());
    }
}
