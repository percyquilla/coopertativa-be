package com.bien.interes.repository;

import com.bien.interes.entity.InteresCapitalizacion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class InteresCapitalizacionRepository implements PanacheRepositoryBase<InteresCapitalizacion, Long> {

    public List<InteresCapitalizacion> listByCuenta(Long cuentaCaptacionId) {
        return list("cuentaCaptacionId = ?1 order by fechaCapitalizacion desc", cuentaCaptacionId);
    }
}
