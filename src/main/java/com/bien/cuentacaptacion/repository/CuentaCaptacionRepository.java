package com.bien.cuentacaptacion.repository;

import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class CuentaCaptacionRepository implements PanacheRepositoryBase<CuentaCaptacion, Long> {

    public boolean existsByNumeroCuenta(String numeroCuenta) {
        return count("numeroCuenta", numeroCuenta) > 0;
    }

    /**
     * Cuentas activas con saldo positivo cuyo producto genera intereses. El
     * devengo de la Fase 6 recorre estas; la existencia de tasa vigente la
     * chequea el servicio.
     */
    public List<CuentaCaptacion> listParaDevengo() {
        return list("estadoCuenta = 'A' and saldoActual > 0 and productoId in "
                + "(select p.id from Producto p where p.permiteInteres = true)");
    }

    public long countByProducto(Long productoId) {
        return count("productoId", productoId);
    }

    /** Cuentas abiertas por producto, para no consultar una vez por fila. */
    public Map<Long, Long> countAgrupadoPorProducto() {
        return getEntityManager()
                .createQuery("select c.productoId, count(c) from CuentaCaptacion c group by c.productoId",
                        Object[].class)
                .getResultStream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
    }
}
