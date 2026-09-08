package com.contabilidad.operacion.repository;

import com.contabilidad.operacion.entity.OperacionContable;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class OperacionContableRepository implements PanacheRepositoryBase<OperacionContable, Long> {

    public long countByEstadoYDia(String estado, LocalDate dia) {
        return count("estado = ?1 and fechaOperacion >= ?2 and fechaOperacion < ?3",
                estado, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }

    public List<OperacionContable> listPorEstado(String estado) {
        return list("estado = ?1 order by fechaOperacion desc, id desc", estado);
    }

    public List<OperacionContable> listRecientes() {
        return find("order by fechaOperacion desc, id desc").range(0, 199).list();
    }

    public boolean existsPorEsquema(Long esquemaContableId) {
        return count("esquemaContableId", esquemaContableId) > 0;
    }

    public long contarPendientes() {
        return count("estado", "PND");
    }
}
