package com.bien.beneficiario.service;

import com.bien.beneficiario.dto.BeneficiarioDtos.BeneficiarioRequest;
import com.bien.beneficiario.dto.BeneficiarioDtos.BeneficiarioResponse;
import com.bien.beneficiario.entity.Beneficiario;
import com.bien.beneficiario.repository.BeneficiarioRepository;
import com.bien.common.security.AuditContext;
import com.bien.persona.service.PersonaService;
import com.bien.socio.service.SocioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class BeneficiarioService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    @Inject
    BeneficiarioRepository repository;

    @Inject
    SocioService socioService;

    @Inject
    PersonaService personaService;

    @Inject
    AuditContext audit;

    public List<BeneficiarioResponse> listBySocio(Long socioId) {
        socioService.findEntityOrThrow(socioId);
        return repository.listBySocio(socioId).stream().map(BeneficiarioResponse::from).toList();
    }

    @Transactional
    public BeneficiarioResponse create(Long socioId, BeneficiarioRequest request) {
        socioService.findEntityOrThrow(socioId);
        validar(socioId, request, null);

        Beneficiario b = new Beneficiario();
        b.setSocioId(socioId);
        b.setEstacion(audit.estacion());
        aplicar(b, request);

        repository.persist(b);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(b);
        return BeneficiarioResponse.from(b);
    }

    @Transactional
    public BeneficiarioResponse update(Long socioId, Long id, BeneficiarioRequest request) {
        Beneficiario b = repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Beneficiario no encontrado: " + id));
        if (!b.getSocioId().equals(socioId)) {
            throw new NotFoundException("El beneficiario no pertenece a ese socio.");
        }
        validar(socioId, request, id);
        aplicar(b, request);
        return BeneficiarioResponse.from(b);
    }

    @Transactional
    public void delete(Long socioId, Long id) {
        Beneficiario b = repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Beneficiario no encontrado: " + id));
        if (!b.getSocioId().equals(socioId)) {
            throw new NotFoundException("El beneficiario no pertenece a ese socio.");
        }
        repository.delete(b);
    }

    // ------------------------------------------------------------------

    private void validar(Long socioId, BeneficiarioRequest request, Long excluirId) {
        if (request.personaId() != null) {
            personaService.findEntityOrThrow(request.personaId());
        }
        boolean vigente = request.estado() == null || "V".equals(request.estado());
        if (!vigente) {
            return;
        }
        // el reparto entre beneficiarios vigentes no puede superar el 100 %
        BigDecimal total = repository.sumaVigente(socioId, excluirId).add(request.porcentaje());
        if (total.compareTo(CIEN) > 0) {
            throw new WebApplicationException(
                    "Los porcentajes de los beneficiarios sumarían " + total.stripTrailingZeros().toPlainString()
                            + " %. El total no puede pasar de 100 %.",
                    Response.Status.CONFLICT);
        }
    }

    private void aplicar(Beneficiario b, BeneficiarioRequest r) {
        b.setPersonaId(r.personaId());
        b.setNombreCompleto(r.nombreCompleto());
        b.setParentesco(r.parentesco());
        b.setPorcentaje(r.porcentaje());
        b.setDocumentoIdentidad(r.documentoIdentidad());
        b.setFechaNacimiento(r.fechaNacimiento());
        b.setEstado(r.estado() != null ? r.estado() : "V");
        b.setUsuario(audit.actor());
        b.setEstacion(audit.estacion());
    }
}
