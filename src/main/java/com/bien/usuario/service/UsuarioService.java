package com.bien.usuario.service;

import com.bien.persona.repository.PersonaRepository;
import com.bien.usuario.dto.UsuarioPasswordRequest;
import com.bien.usuario.dto.UsuarioRequest;
import com.bien.usuario.dto.UsuarioResponse;
import com.bien.usuario.dto.UsuarioUpdateRequest;
import com.bien.usuario.entity.Usuario;
import com.bien.usuario.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PersonaRepository personaRepository;

    @Inject
    PasswordHasher passwordHasher;

    public List<UsuarioResponse> listAll() {
        return usuarioRepository.listAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse findById(Long id) {
        return UsuarioResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new WebApplicationException("El usuario ya existe: " + request.username(), Response.Status.CONFLICT);
        }

        validatePersona(request.personaId(), null);

        String salt = passwordHasher.generateSalt();
        String hash = passwordHasher.hash(request.password(), salt);

        Usuario usuario = new Usuario();
        usuario.setPersonaId(request.personaId());
        usuario.setUsername(request.username());
        usuario.setSalt(salt);
        usuario.setPassword(hash);
        usuario.setRol(request.rol());
        usuario.setActivo(true);

        usuarioRepository.persist(usuario);
        usuarioRepository.getEntityManager().flush();
        usuarioRepository.getEntityManager().refresh(usuario);

        return UsuarioResponse.from(usuario);
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
        usuario.setUsername(request.username());
        usuario.setRol(request.rol());
        return UsuarioResponse.from(usuario);
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
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void changePassword(Long id, UsuarioPasswordRequest request) {
        Usuario usuario = findEntityOrThrow(id);
        String salt = passwordHasher.generateSalt();
        String hash = passwordHasher.hash(request.password(), salt);
        usuario.setSalt(salt);
        usuario.setPassword(hash);
    }

    private Usuario findEntityOrThrow(Long id) {
        return usuarioRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }
}
