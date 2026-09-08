package com.contabilidad.cuentaafectable.repository;

import com.contabilidad.cuentaafectable.entity.CuentaAfectable;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class CuentaAfectableRepository implements PanacheRepositoryBase<CuentaAfectable, Long> {

    public List<CuentaAfectable> listByIds(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : list("id in ?1", ids);
    }

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }

    public boolean existsByCodigoAndIdNot(String codigo, Long id) {
        return count("codigo = ?1 and id <> ?2", codigo, id) > 0;
    }

    public List<CuentaAfectable> listByCuenta(Long cuentaId) {
        return list("cuentaId = ?1 order by codigo", cuentaId);
    }

    public List<CuentaAfectable> listOrdenadas() {
        return list("order by codigo");
    }
}
