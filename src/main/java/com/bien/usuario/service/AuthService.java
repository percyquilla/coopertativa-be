package com.bien.usuario.service;

import com.bien.usuario.dto.LoginRequest;
import com.bien.usuario.dto.LoginResponse;
import com.bien.usuario.entity.Usuario;
import com.bien.usuario.repository.UsuarioRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PasswordHasher passwordHasher;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "bienbcb.jwt.duration-minutes")
    long durationMinutes;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new NotAuthorizedException("Credenciales invalidas"));

        if (!passwordHasher.matches(request.password(), usuario.getSalt(), usuario.getPassword())) {
            throw new NotAuthorizedException("Credenciales invalidas");
        }

        Duration duration = Duration.ofMinutes(durationMinutes);
        String token = Jwt.issuer(issuer)
                .upn(usuario.getUsername())
                .groups(Set.of(usuario.getRol()))
                .expiresIn(duration)
                .sign();

        return new LoginResponse(token, "Bearer", duration.toSeconds());
    }
}
