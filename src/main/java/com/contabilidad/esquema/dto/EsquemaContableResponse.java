package com.contabilidad.esquema.dto;

import com.contabilidad.esquema.entity.EsquemaContable;
import com.contabilidad.esquema.entity.EsquemaContableDetalle;

import java.time.LocalDate;
import java.util.List;

/**
 * @param tieneOperaciones true si ya generó asientos; en ese caso el detalle no
 *                         se edita, se crea una versión nueva.
 */
public record EsquemaContableResponse(
        Long id,
        String codigo,
        String nombre,
        Integer version,
        String descripcion,
        String estado,
        boolean vigente,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean tieneOperaciones,
        List<Detalle> detalle) {

    public record Detalle(
            Long id,
            Integer secuencia,
            String domDebeHaber,
            Long afectableId,
            String afectableCodigo,
            String cuentaNombre,
            String formula,
            String descripcion) {

        public static Detalle from(EsquemaContableDetalle d, String afectableCodigo, String cuentaNombre) {
            return new Detalle(d.getId(), d.getSecuencia(), d.getDomDebeHaber(), d.getAfectableId(),
                    afectableCodigo, cuentaNombre, d.getFormula(), d.getDescripcion());
        }
    }

    public static EsquemaContableResponse from(EsquemaContable e, boolean tieneOperaciones,
                                               List<Detalle> detalle) {
        return new EsquemaContableResponse(
                e.getId(), e.getCodigo(), e.getNombre(), e.getVersion(), e.getDescripcion(),
                e.getEstado(), e.estaVigente(LocalDate.now()), e.getFechaInicio(), e.getFechaFin(),
                tieneOperaciones, detalle);
    }
}
