package com.contabilidad.clasificacioncuenta.repository;

import com.contabilidad.clasificacioncuenta.entity.ClasificacionCuenta;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClasificacionCuentaRepository implements PanacheRepositoryBase<ClasificacionCuenta, Long> {
}
