package com.bien.tipocambio.service;

import com.bien.moneda.repository.MonedaRepository;
import com.bien.tipocambio.dto.TipoCambioRequest;
import com.bien.tipocambio.dto.TipoCambioResponse;
import com.bien.tipocambio.entity.TipoCambio;
import com.bien.tipocambio.repository.TipoCambioRepository;
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
public class TipoCambioService {

    @Inject
    AuditContext audit;

    @Inject
    TipoCambioRepository repository;

    @Inject
    MonedaRepository monedaRepository;

    public List<TipoCambioResponse> listAll() {
        return repository.list("order by fecha desc, id desc").stream()
                .map(TipoCambioResponse::from)
                .toList();
    }

    public TipoCambioResponse findById(Long id) {
        return TipoCambioResponse.from(findEntityOrThrow(id));
    }

    @Transactional
    public TipoCambioResponse create(TipoCambioRequest request) {
        validate(request, null);

        TipoCambio t = new TipoCambio();
        t.setMonedaId(request.monedaId());
        t.setMonedaDestinoId(request.monedaDestinoId());
        t.setFecha(request.fecha());
        t.setCompra(request.compra());
        t.setVenta(request.venta());
        t.setOficial(request.oficial());
        t.setUsuario(audit.actor());
        t.setEstado("V");
        t.setEstacion("SISTEMA");
        repository.persist(t);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(t);
        return TipoCambioResponse.from(t);
    }

    @Transactional
    public TipoCambioResponse update(Long id, TipoCambioRequest request) {
        TipoCambio t = findEntityOrThrow(id);
        validate(request, id);

        t.setMonedaId(request.monedaId());
        t.setMonedaDestinoId(request.monedaDestinoId());
        t.setFecha(request.fecha());
        t.setCompra(request.compra());
        t.setVenta(request.venta());
        t.setOficial(request.oficial());
        t.setUsuario(audit.actor());
        return TipoCambioResponse.from(t);
    }

    private void validate(TipoCambioRequest request, Long id) {
        if (request.monedaId().equals(request.monedaDestinoId())) {
            throw new BadRequestException("La moneda cotizada y la de destino no pueden ser la misma");
        }
        if (monedaRepository.findByIdOptional(request.monedaId()).isEmpty()) {
            throw new NotFoundException("Moneda no encontrada: " + request.monedaId());
        }
        if (monedaRepository.findByIdOptional(request.monedaDestinoId()).isEmpty()) {
            throw new NotFoundException("Moneda no encontrada: " + request.monedaDestinoId());
        }
        boolean duplicate = id == null
                ? repository.existsByClave(request.monedaId(), request.monedaDestinoId(), request.fecha())
                : repository.existsByClaveAndIdNot(request.monedaId(), request.monedaDestinoId(), request.fecha(), id);
        if (duplicate) {
            throw new WebApplicationException(
                    "Ya existe una cotización para ese par de monedas y fecha", Response.Status.CONFLICT);
        }
    }

    private TipoCambio findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Tipo de cambio no encontrado: " + id));
    }
}
