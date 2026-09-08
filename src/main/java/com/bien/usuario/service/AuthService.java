package com.bien.usuario.service;

import com.bien.parametros.service.ParametroLookup;
import com.bien.usuario.dto.LoginRequest;
import com.bien.usuario.dto.LoginResponse;
import com.bien.usuario.entity.Rol;
import com.bien.usuario.entity.Usuario;
import com.bien.usuario.entity.UsuarioRol;
import com.bien.usuario.repository.RolRepository;
import com.bien.usuario.repository.UsuarioRepository;
import com.bien.usuario.repository.UsuarioRolRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    RolRepository rolRepository;

    @Inject
    UsuarioRolRepository usuarioRolRepository;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    LoginLedger loginLedger;

    @Inject
    ParametroLookup parametros;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "bienbcb.jwt.duration-minutes")
    long durationMinutes;

    /**
     * No es {@code @Transactional}: las escrituras de intentos van por
     * {@link LoginLedger} en su propia transacción, para que el incremento del
     * contador de fallidos sobreviva a la excepción de "credenciales inválidas".
     */
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new WebApplicationException("Credenciales inválidas",
                        Response.Status.UNAUTHORIZED));

        if ("B".equals(usuario.getEstado())) {
            throw new WebApplicationException(
                    "El usuario está bloqueado por superar los intentos de ingreso permitidos. "
                            + "Pedí al administrador que lo reactive.",
                    Response.Status.UNAUTHORIZED);
        }
        if (!"V".equals(usuario.getEstado())) {
            throw new WebApplicationException("El usuario está suspendido. Contactá al administrador.",
                    Response.Status.UNAUTHORIZED);
        }

        if (!passwordHasher.matches(request.password(), usuario.getPasswordHash())) {
            int maxIntentos = parametros.intValue("MAX_INTENTOS_LOGIN", 3);
            loginLedger.registrarFallo(usuario.getId(), maxIntentos);
            throw new WebApplicationException("Credenciales inválidas", Response.Status.UNAUTHORIZED);
        }

        Set<String> roles = resolveRoles(usuario.getId());
        if (roles.isEmpty()) {
            throw new WebApplicationException("El usuario no tiene roles asignados", Response.Status.UNAUTHORIZED);
        }

        int diasVigencia = parametros.intValue("DIAS_VIGENCIA_PWD", 90);
        boolean passwordVencida = diasVigencia > 0
                && usuario.getFechaCambioPwd() != null
                && usuario.getFechaCambioPwd().plusDays(diasVigencia).isBefore(LocalDateTime.now());

        boolean requiereCambioPwd = loginLedger.registrarAccesoOk(usuario.getId(), passwordVencida);

        Duration duration = Duration.ofMinutes(durationMinutes);
        String token = Jwt.issuer(issuer)
                .upn(usuario.getUsername())
                .groups(roles)
                .expiresIn(duration)
                .sign();

        return new LoginResponse(token, "Bearer", duration.toSeconds(), requiereCambioPwd);
    }

    private Set<String> resolveRoles(Long usuarioId) {
        List<UsuarioRol> vigentes = usuarioRolRepository.listVigentesByUsuarioId(usuarioId);
        if (vigentes.isEmpty()) {
            return Set.of();
        }
        Set<Long> rolIds = vigentes.stream().map(UsuarioRol::getRolId).collect(Collectors.toSet());
        return rolRepository.listByIdIn(rolIds).stream()
                .map(Rol::getCodigo)
                .collect(Collectors.toSet());
    }
}
