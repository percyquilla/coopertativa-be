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

    /**
     * Cuentas de ahorro (producto AH) no cerradas en las que la persona figura
     * como titular. Un socio no puede retirar su último certificado mientras
     * tenga alguna (Obs. #11).
     */
    public List<CuentaCaptacion> listAhorroNoCerradasByPersona(Long personaId) {
        return list("estadoCuenta <> 'C' "
                + "and productoId in (select p.id from Producto p where p.tipoProducto = 'AH') "
                + "and id in (select t.cuentaCaptacionId from CuentaTitular t where t.personaId = ?1) "
                + "order by numeroCuenta", personaId);
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
