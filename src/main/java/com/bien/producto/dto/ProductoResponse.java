package com.bien.producto.dto;

import com.bien.producto.entity.Producto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String tipoProducto,
        String moneda,
        BigDecimal saldoMinimo,
        boolean permiteDeposito,
        boolean permiteRetiro,
        boolean permiteInteres,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        /**
         * Cuentas de captación registradas con este producto, incluidas las cerradas
         * (también copiaron tipo y moneda al abrirse, así que también congelan esos campos).
         */
        long cantidadCuentas,
        /** estado 'V' <em>y</em> hoy dentro de la vigencia: es lo que decide si se ofrece. */
        boolean vigente,
        /** `permiteInteres` sin una tasa vigente cargada: el producto promete algo que no puede calcular. */
        boolean sinTasaVigente) {

    public static ProductoResponse from(Producto p, long cantidadCuentas) {
        return from(p, cantidadCuentas, true);
    }

    public static ProductoResponse from(Producto p, long cantidadCuentas, boolean tieneTasaVigente) {
        return new ProductoResponse(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getTipoProducto(),
                p.getMoneda(),
                p.getSaldoMinimo(),
                p.isPermiteDeposito(),
                p.isPermiteRetiro(),
                p.isPermiteInteres(),
                p.getEstado(),
                p.getFechaInicio(),
                p.getFechaFin(),
                cantidadCuentas,
                p.estaVigente(LocalDate.now()),
                p.isPermiteInteres() && !tieneTasaVigente);
    }
}
