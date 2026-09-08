package com.contabilidad.cuentacontable.repository;

import com.contabilidad.cuentacontable.entity.CuentaContable;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class CuentaContableRepository implements PanacheRepositoryBase<CuentaContable, Long> {

    public List<CuentaContable> listByIds(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : list("id in ?1", ids);
    }

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }

    public boolean hasHijos(Long cuentaId) {
        return count("cuentaPadreId", cuentaId) > 0;
    }

    public List<CuentaContable> listOrdenadas() {
        return list("order by codigo");
    }

    public List<CuentaContable> listImputables() {
        return list("tipoCuenta = 'MAY' and estado = 'V' order by codigo");
    }
}
