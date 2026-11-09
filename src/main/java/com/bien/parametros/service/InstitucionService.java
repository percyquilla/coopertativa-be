package com.bien.parametros.service;

import com.bien.parametros.dto.InstitucionRequest;
import com.bien.parametros.dto.InstitucionResponse;
import com.bien.parametros.entity.Institucion;
import com.bien.parametros.repository.InstitucionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class InstitucionService {

    @Inject
    AuditContext audit;

    @Inject
    InstitucionRepository repository;

    public InstitucionResponse get() {
        return InstitucionResponse.from(findSingleOrThrow());
    }

    @Transactional
    public InstitucionResponse update(InstitucionRequest request) {
        Institucion i = findSingleOrThrow();
        i.setRazonSocial(request.razonSocial());
        i.setNombreComercial(request.nombreComercial());
        i.setNit(request.nit());
        i.setMatricula(request.matricula());
        i.setCodigoRegulador(request.codigoRegulador());
        i.setDireccion(request.direccion());
        i.setTelefono(request.telefono());
        i.setCorreo(request.correo());
        i.setMonedaBaseId(request.monedaBaseId());
        i.setRepresentanteLegal(request.representanteLegal());
        i.setFechaFundacion(request.fechaFundacion());
        i.setUsuario(audit.actor());
        i.setEstacion(audit.estacion());
        return InstitucionResponse.from(i);
    }

    private Institucion findSingleOrThrow() {
        return repository.findSingle()
                .orElseThrow(() -> new NotFoundException("No hay institución configurada"));
    }
}
