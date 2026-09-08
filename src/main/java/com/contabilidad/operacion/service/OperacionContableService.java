package com.contabilidad.operacion.service;

import com.contabilidad.comprobante.entity.Comprobante;
import com.contabilidad.comprobante.repository.ComprobanteRepository;
import com.contabilidad.motor.ContabilizadorService;
import com.contabilidad.motor.ContextoContable;
import com.contabilidad.operacion.dto.OperacionContableResponse;
import com.contabilidad.operacion.entity.OperacionContable;
import com.contabilidad.operacion.repository.OperacionContableRepository;
import com.contabilidad.tipooperacion.entity.TipoOperacion;
import com.contabilidad.tipooperacion.repository.TipoOperacionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class OperacionContableService {

    @Inject
    OperacionContableRepository repository;

    @Inject
    TipoOperacionRepository tipoOperacionRepository;

    @Inject
    ComprobanteRepository comprobanteRepository;

    @Inject
    ContabilizadorService contabilizador;

    public List<OperacionContableResponse> listAll(String estado) {
        List<OperacionContable> operaciones = (estado == null || estado.isBlank())
                ? repository.listRecientes()
                : repository.listPorEstado(estado);

        Map<Long, String> tipos = tipoOperacionRepository.listAll().stream()
                .collect(Collectors.toMap(TipoOperacion::getId, TipoOperacion::getCodigo));

        return operaciones.stream().map(o -> toResponse(o, tipos)).toList();
    }

    public OperacionContableResponse findById(Long id) {
        return toResponse(findEntityOrThrow(id), null);
    }

    public long contarPendientes() {
        return repository.contarPendientes();
    }

    /**
     * Reintenta una operación pendiente. El contexto se reconstruye desde las
     * columnas de origen y el monto guardados; hoy la única variable de fórmula
     * es {@code MONTO}, así que alcanza.
     */
    @Transactional
    public OperacionContableResponse reprocesar(Long id) {
        OperacionContable op = findEntityOrThrow(id);
        if (!op.estaPendiente()) {
            throw new WebApplicationException(
                    "La operación " + id + " no está pendiente (estado " + op.getEstado() + ").",
                    Response.Status.CONFLICT);
        }
        String codigo = op.getTipoOperacionId() == null ? null
                : tipoOperacionRepository.findByIdOptional(op.getTipoOperacionId())
                        .map(TipoOperacion::getCodigo).orElse(null);
        if (codigo == null) {
            throw new WebApplicationException(
                    "La operación " + id + " no tiene un tipo de operación resoluble; "
                            + "configurá el tipo antes de reprocesar.", Response.Status.CONFLICT);
        }

        LocalDate fecha = op.getFechaOperacion() == null
                ? LocalDate.now() : op.getFechaOperacion().toLocalDate();
        ContextoContable ctx = new ContextoContable(codigo, fecha, op.getGlosa(),
                op.getOrigenModulo(), op.getOrigenTabla(), op.getOrigenId(),
                Map.of("MONTO", op.getMonto() == null ? BigDecimal.ZERO : op.getMonto()));

        contabilizador.reprocesar(id, ctx);

        // el reproceso corrió en una transacción aparte; hay que releer de la
        // base para no devolver la copia PND que quedó en el contexto
        OperacionContable actualizada = findEntityOrThrow(id);
        repository.getEntityManager().refresh(actualizada);
        return toResponse(actualizada, null);
    }

    public OperacionContable findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Operación contable no encontrada: " + id));
    }

    // ---- internos ----

    private OperacionContableResponse toResponse(OperacionContable o, Map<Long, String> tiposCache) {
        String codigo = o.getTipoOperacionId() == null ? null
                : tiposCache != null ? tiposCache.get(o.getTipoOperacionId())
                : tipoOperacionRepository.findByIdOptional(o.getTipoOperacionId())
                        .map(TipoOperacion::getCodigo).orElse(null);
        Long comprobanteId = comprobanteRepository.findByOperacion(o.getId())
                .map(Comprobante::getId).orElse(null);
        return OperacionContableResponse.from(o, codigo, comprobanteId);
    }
}
