package com.bien.persona.service;

import com.bien.persona.dto.PersonaRequest;
import com.bien.persona.dto.PersonaResponse;
import com.bien.persona.entity.Persona;
import com.bien.persona.repository.PersonaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class PersonaService {

    @Inject
    PersonaRepository personaRepository;

    public List<PersonaResponse> listAll() {
        return personaRepository.listAll().stream()
                .map(PersonaResponse::from)
                .toList();
    }

    public PersonaResponse findById(Long id) {
        return PersonaResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public PersonaResponse create(PersonaRequest request) {
        Persona persona = new Persona();
        applyRequest(persona, request);
        personaRepository.persist(persona);
        personaRepository.getEntityManager().flush();
        personaRepository.getEntityManager().refresh(persona);
        return PersonaResponse.from(persona);
    }

    @Transactional
    public PersonaResponse update(Long id, PersonaRequest request) {
        Persona persona = findEntityOrThrow(id);
        applyRequest(persona, request);
        return PersonaResponse.from(persona);
    }

    private Persona findEntityOrThrow(Long id) {
        return personaRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Persona no encontrada: " + id));
    }

    private void applyRequest(Persona persona, PersonaRequest request) {
        persona.setApellidoPaterno(request.apellidoPaterno());
        persona.setApellidoMaterno(request.apellidoMaterno());
        persona.setApellidoCasada(request.apellidoCasada());
        persona.setNombres(request.nombres());
        persona.setCedulaIdentidad(request.cedulaIdentidad());
        persona.setExpedicion(request.expedicion());
        persona.setGenero(request.genero());
        persona.setLugarTrabajo(request.lugarTrabajo());
        persona.setCargoActual(request.cargoActual());
        persona.setCorreoInstitucional(request.correoInstitucional());
        persona.setCorreoPersonal(request.correoPersonal());
        persona.setNumeroTelefono(request.numeroTelefono());
        persona.setFechaNacimiento(request.fechaNacimiento());
        persona.setEstadoCivil(request.estadoCivil());
        persona.setUsuario(request.usuario());
    }
}
