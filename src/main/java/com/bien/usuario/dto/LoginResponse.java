package com.bien.usuario.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        boolean requiereCambioPwd) {
}
