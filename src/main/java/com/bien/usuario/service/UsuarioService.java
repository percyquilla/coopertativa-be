package com.bien.usuario.service;

import com.bien.common.security.AuditContext;
import com.bien.parametros.service.ParametroLookup;
import com.bien.persona.repository.PersonaRepository;
import com.bien.usuario.dto.UsuarioPasswordRequest;
import com.bien.usuario.dto.UsuarioRequest;
import com.bien.usuario.dto.UsuarioResponse;
import com.bien.usuario.dto.UsuarioUpdateRequest;
import com.bien.usuario.entity.Rol;
import com.bien.usuario.entity.Usuario;
import com.bien.usuario.entity.UsuarioPwdHistorico;
import com.bien.usuario.entity.UsuarioRol;
import com.bien.usuario.repository.RolRepository;
import com.bien.usuario.repository.UsuarioPwdHistoricoRepository;
import com.bien.usuario.repository.UsuarioRepository;
import com.bien.usuario.repository.UsuarioRolRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class UsuarioService {

    /** Cuántas contraseñas anteriores no se pueden reutilizar, si el parámetro no está cargado. */
    private static final int HISTORICO_PASSWORDS_DEFAULT = 5;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    AuditContext audit;

    @Inject
    ParametroLookup parametros;

    @Inject
    PersonaRepository personaRepository;

    @Inject
    RolRepository rolRepository;

    @Inject
    UsuarioRolRepository usuarioRolRepository;

    @Inject
    UsuarioPwdHistoricoRepository usuarioPwdHistoricoRepository;

    @Inject
    PasswordHasher passwordHasher;

    public List<UsuarioResponse> listAll() {
        return usuarioRepository.listAll().stream()
                .map(u -> UsuarioResponse.from(u, resolveRoles(u.getId())))
                .toList();
    }

    public UsuarioResponse findById(Long id) {
        Usuario usuario = findEntityOrThrow(id);
        return UsuarioResponse.from(usuario, resolveRoles(id));
    }

    public UsuarioResponse findByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));
        return UsuarioResponse.from(usuario, resolveRoles(usuario.getId()));
    }

    @Transactional
    public void changeOwnPassword(String username, UsuarioPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));
        changePassword(usuario.getId(), request);
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new WebApplicationException("El usuario ya existe: " + request.username(), Response.Status.CONFLICT);
        }
        validatePersona(request.personaId(), null);
        List<Rol> roles = resolveRolesByCodigo(request.roles());

        Usuario usuario = new Usuario();
        usuario.setPersonaId(request.personaId());
        usuario.setSucursalId(request.sucursalId());
        usuario.setUsername(request.username());
        usuario.setCorreo(request.correo());
        usuario.setPasswordHash(passwordHasher.hash(request.password()));
        usuario.setAlgoritmo("BCRYPT");
        usuario.setRequiereCambioPwd(true);
        usuario.setEstado("V");
        usuario.setEstacion(audit.estacion());

        usuarioRepository.persist(usuario);
        usuarioRepository.getEntityManager().flush();
        usuarioRepository.getEntityManager().refresh(usuario);

        LocalDate hoy = LocalDate.now();
        for (Rol rol : roles) {
            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuarioId(usuario.getId());
            usuarioRol.setRolId(rol.getId());
            usuarioRol.setFechaInicio(hoy);
            usuarioRol.setEstado("V");
            usuarioRol.setEstacion(audit.estacion());
            usuarioRolRepository.persist(usuarioRol);
        }

        recordPasswordHistory(usuario);

        return UsuarioResponse.from(usuario, request.roles());
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = findEntityOrThrow(id);

        if (!usuario.getUsername().equals(request.username())
                && usuarioRepository.existsByUsername(request.username())) {
            throw new WebApplicationException("El usuario ya existe: " + request.username(), Response.Status.CONFLICT);
        }

        validatePersona(request.personaId(), id);

        usuario.setPersonaId(request.personaId());
        usuario.setSucursalId(request.sucursalId());
        usuario.setUsername(request.username());
        usuario.setCorreo(request.correo());

        syncRoles(usuario.getId(), request.roles());

        return UsuarioResponse.from(usuario, request.roles());
    }

    private void validatePersona(Long personaId, Long usuarioId) {
        if (!personaRepository.findByIdOptional(personaId).isPresent()) {
            throw new NotFoundException("Persona no encontrada: " + personaId);
        }

        boolean personaYaAsociada = usuarioId == null
                ? usuarioRepository.existsByPersonaId(personaId)
                : usuarioRepository.existsByPersonaIdAndIdNot(personaId, usuarioId);

        if (personaYaAsociada) {
            throw new WebApplicationException("La persona ya tiene un usuario asociado", Response.Status.CONFLICT);
        }
    }

    @Transactional
    public UsuarioResponse setActivo(Long id, boolean activo) {
        Usuario usuario = findEntityOrThrow(id);
        usuario.setActivo(activo);
        if (activo) {
            // reactivar también levanta un bloqueo por intentos fallidos
            usuario.setIntentosFallidos((short) 0);
            usuario.setFechaBloqueo(null);
            usuario.setMotivoBloqueo(null);
        }
        return UsuarioResponse.from(usuario, resolveRoles(id));
    }

    @Transactional
    public void changePassword(Long id, UsuarioPasswordRequest request) {
        Usuario usuario = findEntityOrThrow(id);

        int historico = parametros.intValue("DIAS_HISTORICO_PWD", HISTORICO_PASSWORDS_DEFAULT);
        boolean reutilizada = usuarioPwdHistoricoRepository.listUltimos(id, historico).stream()
                .anyMatch(h -> passwordHasher.matches(request.password(), h.getPasswordHash()));
        if (reutilizada) {
            throw new WebApplicationException(
                    "No puede reutilizar una de las últimas " + historico + " contraseñas",
                    Response.Status.CONFLICT);
        }

        usuario.setPasswordHash(passwordHasher.hash(request.password()));
        usuario.setAlgoritmo("BCRYPT");
        usuario.setRequiereCambioPwd(false);
        usuario.setFechaCambioPwd(LocalDateTime.now());

        recordPasswordHistory(usuario);
    }

    private void recordPasswordHistory(Usuario usuario) {
        UsuarioPwdHistorico historico = new UsuarioPwdHistorico();
        historico.setUsuarioId(usuario.getId());
        historico.setPasswordHash(usuario.getPasswordHash());
        historico.setAlgoritmo(usuario.getAlgoritmo());
        usuarioPwdHistoricoRepository.persist(historico);
    }

    private List<Rol> resolveRolesByCodigo(Set<String> codigos) {
        List<Rol> roles = rolRepository.listByCodigoIn(codigos);
        if (roles.size() != codigos.size()) {
            Set<String> encontrados = roles.stream().map(Rol::getCodigo).collect(Collectors.toSet());
            Set<String> faltantes = new HashSet<>(codigos);
            faltantes.removeAll(encontrados);
            String lista = String.join(", ", faltantes);
            throw new WebApplicationException(
                    faltantes.size() == 1 ? "No existe el rol " + lista : "No existen los roles: " + lista,
                    Response.Status.BAD_REQUEST);
        }
        return roles;
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

    /** Cierra los usuario_rol vigentes que ya no correspondan y abre los que falten, conservando el historial. */
    private void syncRoles(Long usuarioId, Set<String> codigosDeseados) {
        List<Rol> rolesDeseados = resolveRolesByCodigo(codigosDeseados);
        Set<Long> rolIdsDeseados = rolesDeseados.stream().map(Rol::getId).collect(Collectors.toSet());

        List<UsuarioRol> vigentes = usuarioRolRepository.listVigentesByUsuarioId(usuarioId);
        Set<Long> rolIdsVigentes = vigentes.stream().map(UsuarioRol::getRolId).collect(Collectors.toSet());

        LocalDate hoy = LocalDate.now();
        for (UsuarioRol usuarioRol : vigentes) {
            if (!rolIdsDeseados.contains(usuarioRol.getRolId())) {
                usuarioRol.setFechaFin(hoy);
            }
        }
        for (Long rolId : rolIdsDeseados) {
            if (!rolIdsVigentes.contains(rolId)) {
                UsuarioRol nuevo = new UsuarioRol();
                nuevo.setUsuarioId(usuarioId);
                nuevo.setRolId(rolId);
                nuevo.setFechaInicio(hoy);
                nuevo.setEstado("V");
                nuevo.setEstacion(audit.estacion());
                usuarioRolRepository.persist(nuevo);
            }
        }
    }

    private Usuario findEntityOrThrow(Long id) {
        return usuarioRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }
}
