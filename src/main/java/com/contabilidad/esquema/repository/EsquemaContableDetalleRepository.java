package com.contabilidad.esquema.repository;

import com.contabilidad.esquema.entity.EsquemaContableDetalle;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class EsquemaContableDetalleRepository
        implements PanacheRepositoryBase<EsquemaContableDetalle, Long> {

    public List<EsquemaContableDetalle> listByEsquema(Long esquemaContableId) {
        return list("esquemaContableId = ?1 order by secuencia", esquemaContableId);
    }

    public long deleteByEsquema(Long esquemaContableId) {
        return delete("esquemaContableId", esquemaContableId);
    }

    public Map<Long, List<EsquemaContableDetalle>> agrupadosPorEsquema(List<Long> esquemaIds) {
        if (esquemaIds.isEmpty()) {
            return Map.of();
        }
        return list("esquemaContableId in ?1 order by secuencia", esquemaIds).stream()
                .collect(Collectors.groupingBy(EsquemaContableDetalle::getEsquemaContableId));
    }
}
