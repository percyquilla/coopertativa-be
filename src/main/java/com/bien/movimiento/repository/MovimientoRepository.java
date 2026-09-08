package com.bien.movimiento.repository;

import com.bien.movimiento.entity.Movimiento;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class MovimientoRepository implements PanacheRepositoryBase<Movimiento, Long> {

    public List<Movimiento> listByCuenta(Long cuentaCaptacionId) {
        return list("cuentaCaptacionId = ?1 order by fechaHora desc, id desc", cuentaCaptacionId);
    }
}
