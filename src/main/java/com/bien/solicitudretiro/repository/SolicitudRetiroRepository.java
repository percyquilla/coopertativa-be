package com.bien.solicitudretiro.repository;

import com.bien.solicitudretiro.entity.SolicitudRetiro;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SolicitudRetiroRepository implements PanacheRepositoryBase<SolicitudRetiro, Long> {

    public List<SolicitudRetiro> listBySocio(Long socioId) {
        return list("socioId = ?1 order by fechaSolicitud desc, id desc", socioId);
    }

    /** Una solicitud abierta (pendiente o aprobada sin pagar) bloquea otra nueva. */
    public boolean existsAbiertaBySocio(Long socioId) {
        return count("socioId = ?1 and estado in ('PND', 'APR')", socioId) > 0;
    }
}
