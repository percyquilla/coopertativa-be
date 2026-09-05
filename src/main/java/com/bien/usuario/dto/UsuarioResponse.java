package com.bien.usuario.dto;

import com.bien.usuario.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        Long personaId,
        String username,
        String rol,
        boolean activo,
        LocalDateTime fechaHora) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getPersonaId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getFechaHora());
    }
}
