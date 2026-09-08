package com.bien.producto.repository;

import com.bien.producto.entity.Producto;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public class ProductoRepository implements PanacheRepositoryBase<Producto, Long> {

    public Optional<Producto> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }

    public boolean existsByCodigoAndIdNot(String codigo, Long id) {
        return count("codigo = ?1 and id <> ?2", codigo, id) > 0;
    }

    /** estado 'V' + la fecha dentro de la vigencia; ver {@link Producto#estaVigente}. */
    private static final String VIGENTE =
            "estado = 'V' and (fechaInicio is null or fechaInicio <= ?1) "
                    + "and (fechaFin is null or fechaFin >= ?1)";

    /** Primer producto vigente del tipo dado (p. ej. "CA" para aportaciones). */
    public Optional<Producto> findVigenteByTipo(String tipoProducto) {
        return find("tipoProducto = ?2 and " + VIGENTE + " order by codigo",
                LocalDate.now(), tipoProducto).firstResultOptional();
    }

    /**
     * ¿Hay otro producto vigente del mismo tipo? Los módulos que resuelven el
     * producto por tipo (aportaciones → "CA") toman el primero, así que dos
     * vigentes del mismo tipo dejarían la elección al azar.
     */
    public boolean existsVigenteByTipoAndIdNot(String tipoProducto, Long id) {
        return count("tipoProducto = ?2 and id <> ?3 and " + VIGENTE,
                LocalDate.now(), tipoProducto, id == null ? -1L : id) > 0;
    }
}
