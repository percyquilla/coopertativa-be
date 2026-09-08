package com.contabilidad.tipocomprobante.service;

import com.contabilidad.tipocomprobante.dto.TipoComprobanteResponse;
import com.contabilidad.tipocomprobante.entity.TipoComprobante;
import com.contabilidad.tipocomprobante.repository.TipoComprobanteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Catálogo de solo lectura: los 4 tipos (DIA/EGR/ING/TRA) los siembra la
 * migración V7. Agregar uno nuevo no habilita nada por sí solo — la serie de
 * numeración se crea sola al autorizar el primer comprobante del tipo.
 */
@ApplicationScoped
public class TipoComprobanteService {

    @Inject
    TipoComprobanteRepository repository;

    public List<TipoComprobanteResponse> listAll() {
        return repository.listOrdenados().stream()
                .map(TipoComprobanteResponse::from)
                .toList();
    }

    public TipoComprobanteResponse findById(Long id) {
        return TipoComprobanteResponse.from(findEntityOrThrow(id));
    }

    public TipoComprobante findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Tipo de comprobante no encontrado: " + id));
    }

    /** El tipo debe estar vigente para emitir; los suspendidos conservan su historia. */
    public TipoComprobante findVigenteOrThrow(Long id) {
        TipoComprobante t = findEntityOrThrow(id);
        if (!"V".equals(t.getEstado())) {
            throw new WebApplicationException(
                    "El tipo de comprobante " + t.getCodigo() + " no está vigente.",
                    Response.Status.CONFLICT);
        }
        return t;
    }

    public TipoComprobante findByCodigoOrThrow(String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new NotFoundException("Tipo de comprobante no encontrado: " + codigo));
    }
}
