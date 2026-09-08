package com.contabilidad.esquema.repository;

import com.contabilidad.esquema.entity.EsquemaContable;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EsquemaContableRepository implements PanacheRepositoryBase<EsquemaContable, Long> {

    public List<EsquemaContable> listOrdenados() {
        return list("order by codigo, version desc");
    }

    /** La versión vigente de un esquema para una fecha. Debería haber una sola. */
    public Optional<EsquemaContable> findVigente(String codigo, LocalDate fecha) {
        return find("codigo = ?1 and estado = 'V' "
                        + "and (fechaInicio is null or fechaInicio <= ?2) "
                        + "and (fechaFin is null or fechaFin >= ?2) order by version desc",
                codigo, fecha).firstResultOptional();
    }

    public int maxVersion(String codigo) {
        Integer max = find("codigo", codigo).stream()
                .map(EsquemaContable::getVersion)
                .max(Integer::compareTo)
                .orElse(0);
        return max;
    }

    public boolean existsVigenteDistintaVersion(String codigo, Integer version) {
        return count("codigo = ?1 and estado = 'V' and version <> ?2", codigo, version) > 0;
    }
}
