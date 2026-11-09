package com.bien.personaficha.service;

import com.bien.common.security.AuditContext;
import com.bien.persona.service.PersonaService;
import com.bien.personaficha.dto.PersonaFichaDtos.ContactoRequest;
import com.bien.personaficha.dto.PersonaFichaDtos.ContactoResponse;
import com.bien.personaficha.dto.PersonaFichaDtos.DireccionRequest;
import com.bien.personaficha.dto.PersonaFichaDtos.DireccionResponse;
import com.bien.personaficha.dto.PersonaFichaDtos.DocumentoRequest;
import com.bien.personaficha.dto.PersonaFichaDtos.DocumentoResponse;
import com.bien.personaficha.entity.PersonaContacto;
import com.bien.personaficha.entity.PersonaDireccion;
import com.bien.personaficha.entity.PersonaDocumento;
import com.bien.personaficha.repository.PersonaFichaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class PersonaFichaService {


    @Inject
    PersonaService personaService;

    @Inject
    PersonaFichaRepository.Direcciones direcciones;

    @Inject
    PersonaFichaRepository.Contactos contactos;

    @Inject
    PersonaFichaRepository.Documentos documentos;

    @Inject
    AuditContext audit;

    // ---------- direcciones ----------

    public List<DireccionResponse> listDirecciones(Long personaId) {
        personaService.findEntityOrThrow(personaId);
        return direcciones.listByPersona(personaId).stream().map(DireccionResponse::from).toList();
    }

    @Transactional
    public DireccionResponse crearDireccion(Long personaId, DireccionRequest request) {
        personaService.findEntityOrThrow(personaId);
        PersonaDireccion d = new PersonaDireccion();
        d.setPersonaId(personaId);
        d.setEstacion(audit.estacion());
        aplicar(d, request);
        direcciones.persist(d);
        direcciones.getEntityManager().flush();
        if (d.isEsPrincipal()) {
            direcciones.quitarPrincipal(personaId, d.getId());
        }
        return DireccionResponse.from(d);
    }

    @Transactional
    public DireccionResponse actualizarDireccion(Long personaId, Long id, DireccionRequest request) {
        PersonaDireccion d = direcciones.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada: " + id));
        requierePersona(d.getPersonaId(), personaId, "La dirección");
        aplicar(d, request);
        if (d.isEsPrincipal()) {
            direcciones.getEntityManager().flush();
            direcciones.quitarPrincipal(personaId, id);
        }
        return DireccionResponse.from(d);
    }

    @Transactional
    public void borrarDireccion(Long personaId, Long id) {
        PersonaDireccion d = direcciones.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada: " + id));
        requierePersona(d.getPersonaId(), personaId, "La dirección");
        direcciones.delete(d);
    }

    private void aplicar(PersonaDireccion d, DireccionRequest r) {
        d.setTipo(r.tipo());
        d.setDepartamento(r.departamento());
        d.setCiudad(r.ciudad());
        d.setZona(r.zona());
        d.setDireccion(r.direccion());
        d.setReferencia(r.referencia());
        d.setEsPrincipal(r.esPrincipal());
        d.setEstado(r.estado() != null ? r.estado() : "V");
        d.setUsuario(audit.actor());
        d.setEstacion(audit.estacion());
    }

    // ---------- contactos ----------

    public List<ContactoResponse> listContactos(Long personaId) {
        personaService.findEntityOrThrow(personaId);
        return contactos.listByPersona(personaId).stream().map(ContactoResponse::from).toList();
    }

    @Transactional
    public ContactoResponse crearContacto(Long personaId, ContactoRequest request) {
        personaService.findEntityOrThrow(personaId);
        PersonaContacto c = new PersonaContacto();
        c.setPersonaId(personaId);
        c.setEstacion(audit.estacion());
        aplicar(c, request);
        contactos.persist(c);
        contactos.getEntityManager().flush();
        if (c.isEsPrincipal()) {
            contactos.quitarPrincipal(personaId, c.getTipo(), c.getId());
        }
        return ContactoResponse.from(c);
    }

    @Transactional
    public ContactoResponse actualizarContacto(Long personaId, Long id, ContactoRequest request) {
        PersonaContacto c = contactos.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Contacto no encontrado: " + id));
        requierePersona(c.getPersonaId(), personaId, "El contacto");
        aplicar(c, request);
        if (c.isEsPrincipal()) {
            contactos.getEntityManager().flush();
            contactos.quitarPrincipal(personaId, c.getTipo(), id);
        }
        return ContactoResponse.from(c);
    }

    @Transactional
    public void borrarContacto(Long personaId, Long id) {
        PersonaContacto c = contactos.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Contacto no encontrado: " + id));
        requierePersona(c.getPersonaId(), personaId, "El contacto");
        contactos.delete(c);
    }

    private void aplicar(PersonaContacto c, ContactoRequest r) {
        c.setTipo(r.tipo());
        c.setValor(r.valor());
        c.setEsPrincipal(r.esPrincipal());
        c.setEstado(r.estado() != null ? r.estado() : "V");
        c.setUsuario(audit.actor());
        c.setEstacion(audit.estacion());
    }

    // ---------- documentos ----------

    public List<DocumentoResponse> listDocumentos(Long personaId) {
        personaService.findEntityOrThrow(personaId);
        return documentos.listByPersona(personaId).stream().map(DocumentoResponse::from).toList();
    }

    @Transactional
    public DocumentoResponse crearDocumento(Long personaId, DocumentoRequest request) {
        personaService.findEntityOrThrow(personaId);
        validarFechas(request);
        PersonaDocumento d = new PersonaDocumento();
        d.setPersonaId(personaId);
        d.setEstacion(audit.estacion());
        aplicar(d, request);
        documentos.persist(d);
        documentos.getEntityManager().flush();
        documentos.getEntityManager().refresh(d);
        return DocumentoResponse.from(d);
    }

    @Transactional
    public DocumentoResponse actualizarDocumento(Long personaId, Long id, DocumentoRequest request) {
        PersonaDocumento d = documentos.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Documento no encontrado: " + id));
        requierePersona(d.getPersonaId(), personaId, "El documento");
        validarFechas(request);
        aplicar(d, request);
        return DocumentoResponse.from(d);
    }

    @Transactional
    public void borrarDocumento(Long personaId, Long id) {
        PersonaDocumento d = documentos.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Documento no encontrado: " + id));
        requierePersona(d.getPersonaId(), personaId, "El documento");
        documentos.delete(d);
    }

    private void aplicar(PersonaDocumento d, DocumentoRequest r) {
        d.setTipo(r.tipo());
        d.setNumero(r.numero());
        d.setDescripcion(r.descripcion());
        d.setRutaArchivo(r.rutaArchivo());
        d.setFechaEmision(r.fechaEmision());
        d.setFechaVencimiento(r.fechaVencimiento());
        d.setEstado(r.estado() != null ? r.estado() : "V");
        d.setUsuario(audit.actor());
        d.setEstacion(audit.estacion());
    }

    private void validarFechas(DocumentoRequest r) {
        if (r.fechaEmision() != null && r.fechaVencimiento() != null
                && r.fechaVencimiento().isBefore(r.fechaEmision())) {
            throw new WebApplicationException(
                    "La fecha de vencimiento no puede ser anterior a la de emisión.",
                    Response.Status.BAD_REQUEST);
        }
    }

    // ---------- común ----------

    private void requierePersona(Long propietario, Long personaId, String queCosa) {
        if (!propietario.equals(personaId)) {
            throw new NotFoundException(queCosa + " no pertenece a esa persona.");
        }
    }
}
