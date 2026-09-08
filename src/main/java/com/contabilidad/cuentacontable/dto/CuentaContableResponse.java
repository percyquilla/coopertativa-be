package com.contabilidad.cuentacontable.dto;

import com.contabilidad.cuentacontable.entity.CuentaContable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CuentaContableResponse(
        Long id,
        Long cuentaPadreId,
        String cuentaPadreCodigo,
        Long clasificacionId,
        String clasificacionCodigo,
        String naturaleza,
        String tipoCuenta,
        String codigo,
        String nombre,
        String descripcion,
        Short nivel,
        boolean tieneHijos,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime fechaHora) {

    public static CuentaContableResponse from(CuentaContable c,
                                              String cuentaPadreCodigo,
                                              String clasificacionCodigo,
                                              String naturaleza,
                                              boolean tieneHijos) {
        return new CuentaContableResponse(
                c.getId(),
                c.getCuentaPadreId(),
                cuentaPadreCodigo,
                c.getClasificacionId(),
                clasificacionCodigo,
                naturaleza,
                c.getTipoCuenta(),
                c.getCodigo(),
                c.getNombre(),
                c.getDescripcion(),
                c.getNivel(),
                tieneHijos,
                c.getEstado(),
                c.getFechaInicio(),
                c.getFechaFin(),
                c.getFechaHora());
    }
}
