package com.bien.usuario.dto;

import com.bien.usuario.entity.Usuario;

import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioResponse(
        Long id,
        Long personaId,
        Long sucursalId,
        String username,
        String correo,
        Set<String> roles,
        boolean activo,
        boolean requiereCambioPwd,
        LocalDateTime fechaUltimoAcceso,
        LocalDateTime fechaHora) {

    public static UsuarioResponse from(Usuario usuario, Set<String> roles) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getPersonaId(),
                usuario.getSucursalId(),
                usuario.getUsername(),
                usuario.getCorreo(),
                roles,
                usuario.isActivo(),
                usuario.isRequiereCambioPwd(),
                usuario.getFechaUltimoAcceso(),
                usuario.getFechaHora());
    }
}
