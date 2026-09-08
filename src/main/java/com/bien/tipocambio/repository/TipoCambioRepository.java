package com.bien.tipocambio.repository;

import com.bien.tipocambio.entity.TipoCambio;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

@ApplicationScoped
public class TipoCambioRepository implements PanacheRepositoryBase<TipoCambio, Long> {

    public boolean existsByClave(Long monedaId, Long monedaDestinoId, LocalDate fecha) {
        return count("monedaId = ?1 and monedaDestinoId = ?2 and fecha = ?3", monedaId, monedaDestinoId, fecha) > 0;
    }

    public boolean existsByClaveAndIdNot(Long monedaId, Long monedaDestinoId, LocalDate fecha, Long id) {
        return count("monedaId = ?1 and monedaDestinoId = ?2 and fecha = ?3 and id <> ?4",
                monedaId, monedaDestinoId, fecha, id) > 0;
    }
}
