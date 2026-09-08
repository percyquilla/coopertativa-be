package com.contabilidad.periodocontable.repository;

import com.contabilidad.periodocontable.entity.PeriodoContable;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PeriodoContableRepository implements PanacheRepositoryBase<PeriodoContable, Long> {

    public List<PeriodoContable> listOrdenados() {
        return list("order by gestion desc, mes desc");
    }

    public boolean existsGestion(Integer gestion) {
        return count("gestion", gestion) > 0;
    }

    /** ¿Queda algún período anterior de la misma gestión sin cerrar? */
    public boolean existsAnteriorAbierto(Integer gestion, Short mes) {
        return count("gestion = ?1 and mes < ?2 and estado <> 'C'", gestion, mes) > 0;
    }

    /** Período que contiene la fecha dada — el que corresponde a un asiento. */
    public Optional<PeriodoContable> findByFecha(LocalDate fecha) {
        return find("fechaInicio <= ?1 and fechaFin >= ?1", fecha).firstResultOptional();
    }

    /** El mes calendario anterior: mes−1 de la misma gestión, o diciembre de la gestión previa. */
    public Optional<PeriodoContable> findAnterior(Integer gestion, Short mes) {
        return mes > 1
                ? find("gestion = ?1 and mes = ?2", gestion, (short) (mes - 1)).firstResultOptional()
                : find("gestion = ?1 and mes = 12", gestion - 1).firstResultOptional();
    }

    /** El mes calendario siguiente: mes+1 de la misma gestión, o enero de la gestión siguiente. */
    public Optional<PeriodoContable> findSiguiente(Integer gestion, Short mes) {
        return mes < 12
                ? find("gestion = ?1 and mes = ?2", gestion, (short) (mes + 1)).firstResultOptional()
                : find("gestion = ?1 and mes = 1", gestion + 1).firstResultOptional();
    }
}
