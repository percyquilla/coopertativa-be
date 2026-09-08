package com.contabilidad.saldo.repository;

import com.contabilidad.saldo.entity.SaldoCuenta;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SaldoCuentaRepository implements PanacheRepositoryBase<SaldoCuenta, Long> {

    public Optional<SaldoCuenta> findByPeriodoYAfectable(Long periodoId, Long afectableId) {
        return find("periodoId = ?1 and afectableId = ?2", periodoId, afectableId).firstResultOptional();
    }

    public List<SaldoCuenta> listByPeriodo(Long periodoId) {
        return list("periodoId", periodoId);
    }
}
