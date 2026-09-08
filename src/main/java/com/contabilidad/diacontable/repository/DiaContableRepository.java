package com.contabilidad.diacontable.repository;

import com.contabilidad.diacontable.entity.DiaContable;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DiaContableRepository implements PanacheRepositoryBase<DiaContable, Long> {

    public Optional<DiaContable> findByFecha(LocalDate fecha) {
        return find("fecha", fecha).firstResultOptional();
    }

    public List<DiaContable> listByPeriodo(Long periodoId) {
        return list("periodoId = ?1 order by fecha desc", periodoId);
    }

    /** Un día abierto o reabierto que no sea el de {@code fecha}. Solo puede haber uno. */
    public Optional<DiaContable> findOtroAbierto(LocalDate fecha) {
        return find("estado in ('A', 'R') and fecha <> ?1", fecha).firstResultOptional();
    }

    /**
     * El día pendiente de apertura más antiguo anterior a {@code fecha}, dentro de un
     * período no cerrado. Si existe, no se puede abrir {@code fecha} todavía: los días
     * se abren en orden.
     */
    public Optional<DiaContable> findPendienteAnterior(LocalDate fecha) {
        return find("estado = 'P' and fecha < ?1 and periodoId in "
                + "(select p.id from PeriodoContable p where p.estado <> 'C') order by fecha", fecha)
                .firstResultOptional();
    }

    /** Un día ya cerrado posterior a {@code fecha}: no se puede abrir un día "hacia atrás". */
    public Optional<DiaContable> findCerradoPosterior(LocalDate fecha) {
        return find("estado = 'C' and fecha > ?1 order by fecha", fecha).firstResultOptional();
    }
}
