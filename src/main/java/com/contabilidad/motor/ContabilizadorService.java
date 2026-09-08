package com.contabilidad.motor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Punto de entrada del motor para el resto del sistema. Un módulo de negocio
 * (captaciones, aportaciones) llama a {@link #contabilizar} después de grabar su
 * hecho; el motor genera el asiento en una transacción aparte.
 * <p>
 * <b>No bloqueante:</b> si el asiento falla (falta un esquema, el período está
 * cerrado, una afectable fue dada de baja), la operación de negocio ya está
 * grabada y no se toca — el hecho queda en la cola de operaciones pendientes
 * (estado PND) para reprocesar desde la pantalla.
 */
@ApplicationScoped
public class ContabilizadorService {

    private static final Logger LOG = Logger.getLogger(ContabilizadorService.class);

    @Inject
    MotorContableTx tx;

    /**
     * Contabiliza el hecho descrito por {@code ctx}. Nunca lanza: cualquier
     * problema se registra como operación pendiente. Llamar siempre <em>después</em>
     * del {@code flush()} que asigna el id de la fila de origen.
     */
    public void contabilizar(ContextoContable ctx) {
        try {
            tx.generar(ctx, null);
        } catch (RuntimeException e) {
            LOG.warnf("No se pudo contabilizar %s (%s#%s): %s",
                    ctx.codigoOperacion(), ctx.origenTabla(), ctx.origenId(), e.getMessage());
            try {
                tx.registrarPendiente(ctx, e.getMessage(), null);
            } catch (RuntimeException encolar) {
                LOG.errorf(encolar, "No se pudo ni encolar la operación pendiente %s (%s#%s)",
                        ctx.codigoOperacion(), ctx.origenTabla(), ctx.origenId());
            }
        }
    }

    /**
     * Reintenta una operación que quedó pendiente, reutilizando su fila. Éxito →
     * la fila pasa a PROC con su comprobante; fracaso → sigue PND con el nuevo
     * motivo y un intento más. Lanza solo si el reproceso no era aplicable.
     */
    public void reprocesar(Long operacionPendienteId, ContextoContable ctx) {
        try {
            tx.generar(ctx, operacionPendienteId);
        } catch (RuntimeException e) {
            LOG.warnf("Reproceso de la operación %s falló: %s", operacionPendienteId, e.getMessage());
            tx.registrarPendiente(ctx, e.getMessage(), operacionPendienteId);
        }
    }
}
