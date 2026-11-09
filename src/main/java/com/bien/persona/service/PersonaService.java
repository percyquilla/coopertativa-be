package com.bien.persona.service;

import com.bien.dominio.repository.DominioRepository;
import com.bien.persona.dto.PersonaRequest;
import com.bien.persona.dto.PersonaResponse;
import com.bien.persona.entity.Persona;
import com.bien.persona.repository.PersonaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class PersonaService {

    @Inject
    AuditContext audit;

    @Inject
    PersonaRepository personaRepository;

    @Inject
    DominioRepository.DominioValorRepository dominioValorRepository;

    static final String DOM_GENERO = "dom_genero";
    static final String DOM_ESTADO_CIVIL = "dom_estado_civil";

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
        if (personaRepository.existsByCedulaIdentidadAndExpedicion(request.cedulaIdentidad(), request.expedicion())) {
            throw new WebApplicationException("Ya existe una persona con esa cédula de identidad", Response.Status.CONFLICT);
        }
        Persona persona = new Persona();
        applyRequest(persona, request);
        persona.setEstado("V");
        persona.setEstacion(audit.estacion());
        personaRepository.persist(persona);
        personaRepository.getEntityManager().flush();
        personaRepository.getEntityManager().refresh(persona);
        return PersonaResponse.from(persona);
    }

    @Transactional
    public PersonaResponse update(Long id, PersonaRequest request) {
        Persona persona = findEntityOrThrow(id);
        if (personaRepository.existsByCedulaIdentidadAndExpedicionAndIdNot(request.cedulaIdentidad(), request.expedicion(), id)) {
            throw new WebApplicationException("Ya existe una persona con esa cédula de identidad", Response.Status.CONFLICT);
        }
        applyRequest(persona, request);
        persona.setFechaHora(java.time.LocalDateTime.now());
        return PersonaResponse.from(persona);
    }

    public Persona findEntityOrThrow(Long id) {
        return personaRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Persona no encontrada: " + id));
    }

    /** Género y estado civil se guardan por código del dominio, no por descripción. */
    private void validarCodigo(String dominioId, String valor, String campo) {
        if (!dominioValorRepository.existsVigente(dominioId, valor)) {
            throw new BadRequestException("El " + campo + " «" + valor
                    + "» no es un código válido del catálogo " + dominioId + ".");
        }
    }

    private void applyRequest(Persona persona, PersonaRequest request) {
        validarCodigo(DOM_GENERO, request.genero(), "género");
        validarCodigo(DOM_ESTADO_CIVIL, request.estadoCivil(), "estado civil");
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
        persona.setUsuario(audit.actor());
        persona.setEstacion(audit.estacion());
    }
}
