package com.bien.parametros.repository;

import com.bien.parametros.entity.Feriado;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

@ApplicationScoped
public class FeriadoRepository implements PanacheRepositoryBase<Feriado, Long> {

    public boolean existsNacional(LocalDate fecha) {
        return count("fecha = ?1 and sucursalId is null", fecha) > 0;
    }

    public boolean existsNacionalAndIdNot(LocalDate fecha, Long id) {
        return count("fecha = ?1 and sucursalId is null and id <> ?2", fecha, id) > 0;
    }

    public boolean existsSucursal(LocalDate fecha, Long sucursalId) {
        return count("fecha = ?1 and sucursalId = ?2", fecha, sucursalId) > 0;
    }

    public boolean existsSucursalAndIdNot(LocalDate fecha, Long sucursalId, Long id) {
        return count("fecha = ?1 and sucursalId = ?2 and id <> ?3", fecha, sucursalId, id) > 0;
    }
}
