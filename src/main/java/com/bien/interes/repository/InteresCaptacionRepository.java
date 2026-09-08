package com.bien.interes.repository;

import com.bien.interes.entity.InteresCaptacion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class InteresCaptacionRepository implements PanacheRepositoryBase<InteresCaptacion, Long> {

    /** El devengo de una cuenta para el mes que contiene {@code fecha}. */
    public Optional<InteresCaptacion> findDelMes(Long cuentaCaptacionId, LocalDate fecha) {
        LocalDate inicio = fecha.withDayOfMonth(1);
        return find("cuentaCaptacionId = ?1 and fechaInicioPeriodo = ?2", cuentaCaptacionId, inicio)
                .firstResultOptional();
    }

    /** Devengos 'REG' con importe pendiente de capitalizar del mes que contiene {@code fecha}. */
    public List<InteresCaptacion> listRegDelMes(LocalDate fecha) {
        LocalDate inicio = fecha.withDayOfMonth(1);
        return list("fechaInicioPeriodo = ?1 and estado = 'REG' and interesDevengado > 0", inicio);
    }

    public List<InteresCaptacion> listByCuenta(Long cuentaCaptacionId) {
        return list("cuentaCaptacionId = ?1 order by fechaInicioPeriodo desc", cuentaCaptacionId);
    }

    /** Cuántas cuentas tienen su devengo actualizado al día {@code fecha} (para el cierre diario). */
    public long countDevengadosEn(LocalDate fecha) {
        return count("fechaFinPeriodo = ?1", fecha);
    }
}
