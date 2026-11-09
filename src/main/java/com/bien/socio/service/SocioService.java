package com.bien.socio.service;

import com.bien.certificadoaportacion.repository.CertificadoAportacionRepository;
import com.bien.common.security.AuditContext;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.persona.repository.PersonaRepository;
import com.bien.socio.dto.SocioReporteRow;
import com.bien.socio.dto.SocioRequest;
import com.bien.socio.dto.SocioResponse;
import com.bien.socio.dto.SocioUpdateRequest;
import com.bien.socio.entity.Socio;
import com.bien.socio.repository.SocioRepository;
import com.bien.solicitudretiro.repository.SolicitudRetiroRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SocioService {

    @Inject
    AuditContext audit;

    @Inject
    SocioRepository socioRepository;

    @Inject
    PersonaRepository personaRepository;

    @Inject
    CertificadoAportacionRepository certificadoRepository;

    @Inject
    CuentaCaptacionRepository cuentaCaptacionRepository;

    @Inject
    SolicitudRetiroRepository solicitudRetiroRepository;

    public List<SocioResponse> listAll() {
        return socioRepository.listAll().stream()
                .map(SocioResponse::from)
                .toList();
    }

    public SocioResponse findById(Long id) {
        return SocioResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public SocioResponse create(SocioRequest request) {
        if (personaRepository.findByIdOptional(request.personaId()).isEmpty()) {
            throw new NotFoundException("Persona no encontrada: " + request.personaId());
        }
        if (socioRepository.existsByPersonaId(request.personaId())) {
            throw new WebApplicationException("La persona ya es socia", Response.Status.CONFLICT);
        }
        if (socioRepository.existsByNumeroSocio(request.numeroSocio())) {
            throw new WebApplicationException("El número de socio ya existe: " + request.numeroSocio(), Response.Status.CONFLICT);
        }

        Socio socio = new Socio();
        socio.setPersonaId(request.personaId());
        socio.setNumeroSocio(request.numeroSocio());
        socio.setSucursalId(request.sucursalId());
        socio.setFechaIngreso(request.fechaIngreso());
        socio.setUsuario(audit.actor());
        socio.setEstado("V");
        socio.setEstacion(audit.estacion());

        socioRepository.persist(socio);
        socioRepository.getEntityManager().flush();
        socioRepository.getEntityManager().refresh(socio);

        return SocioResponse.from(socio);
    }

    public List<SocioReporteRow> reporte() {
        return socioRepository.reporte();
    }

    /** El número de socio no se modifica (Obs. #4): solo sucursal y fecha de ingreso. */
    @Transactional
    public SocioResponse update(Long id, SocioUpdateRequest request) {
        Socio socio = findEntityOrThrow(id);
        socio.setSucursalId(request.sucursalId());
        socio.setFechaIngreso(request.fechaIngreso());
        socio.setUsuario(audit.actor());
        socio.setEstacion(audit.estacion());
        return SocioResponse.from(socio);
    }

    /**
     * Retiro directo (baja administrativa). Solo procede para un socio sin dinero
     * ni trámites de por medio: si tiene certificados vigentes, saldo en su
     * cuenta de aportaciones (modelo anterior) o una solicitud de retiro en
     * curso, hay que pasar por el flujo formal de solicitud de retiro.
     */
    @Transactional
    public SocioResponse retirar(Long id) {
        Socio socio = findEntityOrThrow(id);
        if (!"V".equals(socio.getEstado())) {
            throw new WebApplicationException("El socio no está vigente; no se puede retirar.",
                    Response.Status.CONFLICT);
        }
        if (solicitudRetiroRepository.existsAbiertaBySocio(id)) {
            throw new WebApplicationException(
                    "El socio ya tiene una solicitud de retiro en curso. Resuélvala desde Solicitudes de retiro.",
                    Response.Status.CONFLICT);
        }
        long vigentes = certificadoRepository.countVigentesBySocio(id);
        if (vigentes > 0) {
            throw new WebApplicationException(
                    "El socio tiene " + vigentes + " certificado(s) de aportación vigente(s). Use una solicitud "
                            + "de retiro para devolverlos.",
                    Response.Status.CONFLICT);
        }
        certificadoRepository.findCuentaCaBySocio(id).ifPresent(cuentaId -> {
            BigDecimal saldo = cuentaCaptacionRepository.findByIdOptional(cuentaId)
                    .map(c -> c.getSaldoActual())
                    .orElse(BigDecimal.ZERO);
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                throw new WebApplicationException(
                        "El socio tiene " + saldo + " en su cuenta de aportaciones. Usá una solicitud de retiro "
                                + "para devolverle el saldo y retirarlo.",
                        Response.Status.CONFLICT);
            }
        });
        socio.setEstado("R");
        socio.setFechaRetiro(LocalDate.now());
        return SocioResponse.from(socio);
    }

    @Transactional
    public SocioResponse reactivar(Long id) {
        Socio socio = findEntityOrThrow(id);
        socio.setEstado("V");
        socio.setFechaRetiro(null);
        return SocioResponse.from(socio);
    }

    public Socio findEntityOrThrow(Long id) {
        return socioRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Socio no encontrado: " + id));
    }
}
