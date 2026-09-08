package com.bien.productotasa.dto;

import com.bien.productotasa.entity.ProductoTasa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoTasaResponse(
        Long id,
        Long productoId,
        BigDecimal tasa,
        String tipoTasa,
        Integer baseDias,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado,
        /** estado 'V' y hoy dentro del rango: es la tasa que se aplicaría. */
        boolean vigente) {

    public static ProductoTasaResponse from(ProductoTasa t) {
        return new ProductoTasaResponse(
                t.getId(),
                t.getProductoId(),
                t.getTasa(),
                t.getTipoTasa(),
                t.getBaseDias(),
                t.getFechaInicio(),
                t.getFechaFin(),
                t.getEstado(),
                t.estaVigente(LocalDate.now()));
    }
}
