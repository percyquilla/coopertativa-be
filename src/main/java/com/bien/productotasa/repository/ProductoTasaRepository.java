package com.bien.productotasa.repository;

import com.bien.productotasa.entity.ProductoTasa;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductoTasaRepository implements PanacheRepositoryBase<ProductoTasa, Long> {

    private static final String VIGENTE =
            "estado = 'V' and fechaInicio <= ?1 and (fechaFin is null or fechaFin >= ?1)";

    public List<ProductoTasa> listByProducto(Long productoId) {
        return list("productoId = ?1 order by fechaInicio desc", productoId);
    }

    /** La tasa que se aplicaría hoy a ese producto. */
    public Optional<ProductoTasa> findVigente(Long productoId, LocalDate fecha) {
        return find("productoId = ?2 and " + VIGENTE + " order by fechaInicio desc", fecha, productoId)
                .firstResultOptional();
    }

    /**
     * ¿Otra tasa vigente del mismo producto se solapa con el rango dado? Dos tasas
     * vigentes a la vez dejarían el cálculo de intereses al azar.
     */
    public boolean existsSolapada(Long productoId, LocalDate desde, LocalDate hasta, Long excluirId) {
        return count("productoId = ?1 and estado = 'V' and id <> ?2 "
                        + "and fechaInicio <= ?3 and (fechaFin is null or fechaFin >= ?4)",
                productoId, excluirId == null ? -1L : excluirId,
                hasta == null ? LocalDate.of(9999, 12, 31) : hasta, desde) > 0;
    }

    /** Productos que hoy tienen al menos una tasa vigente, para marcarlos en la lista. */
    public Map<Long, Long> countVigentesPorProducto(LocalDate fecha) {
        return getEntityManager()
                .createQuery("select t.productoId, count(t) from ProductoTasa t "
                        + "where t.estado = 'V' and t.fechaInicio <= :f "
                        + "and (t.fechaFin is null or t.fechaFin >= :f) group by t.productoId",
                        Object[].class)
                .setParameter("f", fecha)
                .getResultStream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
    }
}
