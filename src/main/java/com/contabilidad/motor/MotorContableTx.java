package com.contabilidad.motor;

import com.bien.common.security.AuditContext;
import com.bien.parametros.entity.Institucion;
import com.bien.parametros.repository.InstitucionRepository;
import com.contabilidad.comprobante.dto.ComprobanteRengRequest;
import com.contabilidad.comprobante.dto.ComprobanteRequest;
import com.contabilidad.comprobante.dto.ComprobanteResponse;
import com.contabilidad.comprobante.entity.Comprobante;
import com.contabilidad.comprobante.service.ComprobanteService;
import com.contabilidad.esquema.entity.EsquemaContable;
import com.contabilidad.esquema.entity.EsquemaContableDetalle;
import com.contabilidad.esquema.repository.EsquemaContableDetalleRepository;
import com.contabilidad.esquema.repository.EsquemaContableRepository;
import com.contabilidad.esquema.service.EsquemaContableService;
import com.contabilidad.operacion.entity.OperacionContable;
import com.contabilidad.operacion.repository.OperacionContableRepository;
import com.contabilidad.tipooperacion.entity.TipoOperacion;
import com.contabilidad.tipooperacion.service.TipoOperacionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Las dos operaciones transaccionales del motor, cada una en su propia
 * transacción ({@code REQUIRES_NEW}) para que un fallo al contabilizar no
 * arrastre la operación de negocio que la disparó. El try/catch que decide
 * cuál llamar está en {@link ContabilizadorService}.
 * <p>
 * Mismo patrón que {@code com.bien.usuario.LoginLedger}.
 */
@ApplicationScoped
public class MotorContableTx {

    @Inject
    AuditContext audit;

    @Inject
    TipoOperacionService tipoOperacionService;

    @Inject
    EsquemaContableService esquemaService;

    @Inject
    EsquemaContableRepository esquemaRepository;

    @Inject
    EsquemaContableDetalleRepository detalleRepository;

    @Inject
    ComprobanteService comprobanteService;

    @Inject
    OperacionContableRepository operacionRepository;

    @Inject
    InstitucionRepository institucionRepository;

    /**
     * Contabiliza el hecho: resuelve tipo de operación y esquema vigentes,
     * evalúa las fórmulas, arma el comprobante, lo autoriza y deja la
     * {@code operacion_contable} en PROC. Todo o nada.
     *
     * @param operacionExistenteId  al reprocesar, el id de la fila PND que se
     *                              reutiliza; {@code null} para un hecho nuevo.
     * @return id de la {@code operacion_contable} en PROC
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Long generar(ContextoContable ctx, Long operacionExistenteId) {
        TipoOperacion tipoOp = tipoOperacionService.findVigentePorCodigoOrThrow(ctx.codigoOperacion());

        EsquemaContable esquemaBase = esquemaService.findEntityOrThrow(requireEsquema(tipoOp, ctx));
        EsquemaContable esquema = esquemaRepository.findVigente(esquemaBase.getCodigo(), ctx.fecha())
                .orElseThrow(() -> new WebApplicationException(
                        "No hay un esquema contable vigente para «" + esquemaBase.getCodigo()
                                + "» en la fecha " + ctx.fecha() + ".", Response.Status.CONFLICT));

        List<EsquemaContableDetalle> detalle = detalleRepository.listByEsquema(esquema.getId());
        if (detalle.isEmpty()) {
            throw new WebApplicationException("El esquema «" + esquema.getCodigo()
                    + "» no tiene renglones configurados.", Response.Status.CONFLICT);
        }

        List<ComprobanteRengRequest> renglones = new ArrayList<>();
        BigDecimal totalDebe = BigDecimal.ZERO;
        int i = 1;
        for (EsquemaContableDetalle d : detalle) {
            BigDecimal monto = EvaluadorFormula
                    .evaluar(d.getFormula(), ctx.variables(),
                            "operación " + ctx.codigoOperacion() + ", renglón " + i)
                    .setScale(2, RoundingMode.HALF_UP);
            i++;
            if (monto.signum() == 0) {
                continue; // un renglón que da cero no se asienta
            }
            if (monto.signum() < 0) {
                throw new WebApplicationException("La fórmula del renglón " + (i - 1)
                        + " del esquema «" + esquema.getCodigo() + "» dio un importe negativo ("
                        + monto.toPlainString() + ").", Response.Status.CONFLICT);
            }
            renglones.add(new ComprobanteRengRequest(d.getAfectableId(), d.getDomDebeHaber(), monto,
                    d.getDescripcion()));
            if ("D".equals(d.getDomDebeHaber())) {
                totalDebe = totalDebe.add(monto);
            }
        }

        OperacionContable op = operacionExistenteId == null
                ? new OperacionContable()
                : operacionRepository.findById(operacionExistenteId);
        if (op == null) {
            throw new WebApplicationException("Operación no encontrada: " + operacionExistenteId,
                    Response.Status.NOT_FOUND);
        }

        op.setTipoOperacionId(tipoOp.getId());
        op.setFechaOperacion(ctx.fecha().atStartOfDay());
        op.setGlosa(recortar(ctx.glosa(), 200));
        op.setMonedaId(monedaBaseOrThrow());
        op.setMonto(totalDebe);
        op.setEstado("PROC");
        op.setEsquemaContableId(esquema.getId());
        op.setOrigenModulo(ctx.origenModulo());
        op.setOrigenTabla(ctx.origenTabla());
        op.setOrigenId(ctx.origenId());
        op.setErrorMensaje(null);
        op.setFechaProceso(LocalDateTime.now());
        op.setIntentos((op.getIntentos() == null ? 0 : op.getIntentos()) + 1);
        op.setUsuario(audit.actor());
        if (op.getEstacion() == null) {
            op.setEstacion(audit.estacion());
        }
        if (operacionExistenteId == null) {
            operacionRepository.persist(op);
        }
        operacionRepository.getEntityManager().flush();

        ComprobanteResponse creado = comprobanteService.create(new ComprobanteRequest(
                tipoOp.getTipoComprobanteId(), ctx.fecha(), recortar(ctx.glosa(), 300), renglones));
        comprobanteService.autorizar(creado.id());

        Comprobante comprobante = comprobanteService.findEntityOrThrow(creado.id());
        comprobante.setOperacionId(op.getId());
        operacionRepository.getEntityManager().flush();

        return op.getId();
    }

    /**
     * Deja constancia de que el hecho no se pudo contabilizar. Fila nueva en PND
     * con el motivo, o —si es un reproceso— actualiza la fila PND existente
     * sumando un intento.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Long registrarPendiente(ContextoContable ctx, String motivo, Long operacionExistenteId) {
        OperacionContable op = operacionExistenteId == null
                ? new OperacionContable()
                : operacionRepository.findById(operacionExistenteId);
        if (op == null) {
            return null;
        }

        TipoOperacion tipoOp = buscarTipoLenient(ctx.codigoOperacion());
        op.setTipoOperacionId(tipoOp == null ? null : tipoOp.getId());
        op.setFechaOperacion(ctx.fecha().atStartOfDay());
        op.setGlosa(recortar(ctx.glosa(), 200));
        op.setMonedaId(monedaBaseOrThrow());
        op.setMonto(ctx.variables().getOrDefault("MONTO", BigDecimal.ZERO));
        op.setEstado("PND");
        op.setOrigenModulo(ctx.origenModulo());
        op.setOrigenTabla(ctx.origenTabla());
        op.setOrigenId(ctx.origenId());
        op.setErrorMensaje(recortar(motivo, 500));
        op.setFechaProceso(LocalDateTime.now());
        op.setIntentos((op.getIntentos() == null ? 0 : op.getIntentos()) + 1);
        op.setUsuario(audit.actor());
        if (op.getEstacion() == null) {
            op.setEstacion(audit.estacion());
        }
        if (operacionExistenteId == null) {
            operacionRepository.persist(op);
        }
        operacionRepository.getEntityManager().flush();
        return op.getId();
    }

    // ---- internos ----

    private Long requireEsquema(TipoOperacion tipoOp, ContextoContable ctx) {
        if (tipoOp.getEsquemaContableId() == null) {
            throw new WebApplicationException("El tipo de operación «" + ctx.codigoOperacion()
                    + "» no tiene esquema contable asignado.", Response.Status.CONFLICT);
        }
        return tipoOp.getEsquemaContableId();
    }

    private TipoOperacion buscarTipoLenient(String codigo) {
        try {
            return tipoOperacionService.findVigentePorCodigoOrThrow(codigo);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Long monedaBaseOrThrow() {
        Long id = institucionRepository.findSingle().map(Institucion::getMonedaBaseId).orElse(null);
        if (id == null) {
            throw new WebApplicationException(
                    "La institución no tiene moneda base configurada.", Response.Status.CONFLICT);
        }
        return id;
    }

    private static String recortar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() > max ? texto.substring(0, max) : texto;
    }
}
