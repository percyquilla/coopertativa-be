package com.bien.cuentatitular.service;

import com.bien.cuentacaptacion.service.CuentaCaptacionService;
import com.bien.cuentatitular.dto.CuentaTitularRequest;
import com.bien.cuentatitular.dto.CuentaTitularResponse;
import com.bien.cuentatitular.entity.CuentaTitular;
import com.bien.cuentatitular.repository.CuentaTitularRepository;
import com.bien.persona.repository.PersonaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CuentaTitularService {

    @Inject
    AuditContext audit;

    @Inject
    CuentaTitularRepository repository;

    @Inject
    CuentaCaptacionService cuentaService;

    @Inject
    PersonaRepository personaRepository;

    public List<CuentaTitularResponse> listByCuenta(Long cuentaCaptacionId) {
        cuentaService.findEntityOrThrow(cuentaCaptacionId);
        return repository.listByCuenta(cuentaCaptacionId).stream()
                .map(CuentaTitularResponse::from)
                .toList();
    }

    @Transactional
    public CuentaTitularResponse agregar(Long cuentaCaptacionId, CuentaTitularRequest request) {
        cuentaService.findEntityOrThrow(cuentaCaptacionId);
        if (personaRepository.findByIdOptional(request.personaId()).isEmpty()) {
            throw new NotFoundException("Persona no encontrada: " + request.personaId());
        }
        if (repository.existsVigente(cuentaCaptacionId, request.personaId())) {
            throw new WebApplicationException("La persona ya es titular vigente de la cuenta", Response.Status.CONFLICT);
        }
        if ("P".equals(request.tipoTitular()) && repository.existsVigentePrincipal(cuentaCaptacionId)) {
            throw new WebApplicationException("La cuenta ya tiene un titular principal vigente", Response.Status.CONFLICT);
        }

        CuentaTitular t = new CuentaTitular();
        t.setCuentaCaptacionId(cuentaCaptacionId);
        t.setPersonaId(request.personaId());
        t.setTipoTitular(request.tipoTitular());
        t.setFechaInicio(LocalDate.now());
        t.setEstado("V");
        t.setUsuario(audit.actor());
        t.setEstacion(audit.estacion());
        repository.persist(t);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(t);
        return CuentaTitularResponse.from(t);
    }

    @Transactional
    public CuentaTitularResponse finalizar(Long cuentaCaptacionId, Long titularId) {
        CuentaTitular t = repository.findByIdOptional(titularId)
                .orElseThrow(() -> new NotFoundException("Titular no encontrado: " + titularId));
        if (!t.getCuentaCaptacionId().equals(cuentaCaptacionId)) {
            throw new NotFoundException("El titular no pertenece a la cuenta " + cuentaCaptacionId);
        }
        if (t.getFechaFin() != null) {
            throw new WebApplicationException("El titular ya está finalizado", Response.Status.CONFLICT);
        }
        t.setFechaFin(LocalDate.now());
        t.setEstado("S");
        return CuentaTitularResponse.from(t);
    }
}
