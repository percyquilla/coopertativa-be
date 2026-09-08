package com.bien.cuentatitular.repository;

import com.bien.cuentatitular.entity.CuentaTitular;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CuentaTitularRepository implements PanacheRepositoryBase<CuentaTitular, Long> {

    public List<CuentaTitular> listByCuenta(Long cuentaCaptacionId) {
        return list("cuentaCaptacionId = ?1 order by fechaInicio, id", cuentaCaptacionId);
    }

    public boolean existsVigente(Long cuentaCaptacionId, Long personaId) {
        return count("cuentaCaptacionId = ?1 and personaId = ?2 and fechaFin is null",
                cuentaCaptacionId, personaId) > 0;
    }

    public boolean existsVigentePrincipal(Long cuentaCaptacionId) {
        return count("cuentaCaptacionId = ?1 and tipoTitular = 'P' and fechaFin is null", cuentaCaptacionId) > 0;
    }
}
