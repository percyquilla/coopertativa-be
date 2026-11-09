package com.bien.solicitudretiro.repository;

import com.bien.solicitudretiro.entity.SolicitudRetiro;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SolicitudRetiroRepository implements PanacheRepositoryBase<SolicitudRetiro, Long> {

    /** Eliminación lógica: no se lista. */
    public static final String ELIMINADA = "ELM";

    public List<SolicitudRetiro> listNoEliminadas() {
        return list("estado <> ?1 order by fechaSolicitud desc, id desc", ELIMINADA);
    }

    public List<SolicitudRetiro> listBySocio(Long socioId) {
        return list("socioId = ?1 and estado <> ?2 order by fechaSolicitud desc, id desc", socioId, ELIMINADA);
    }

    /** Solicitudes en curso (registradas o aprobadas) del socio. */
    public boolean existsAbiertaBySocio(Long socioId) {
        return count("socioId = ?1 and estado in ('REG', 'PND')", socioId) > 0;
    }

    /**
     * Una solicitud en curso (registrada o aprobada) sobre el certificado
     * bloquea otra. {@code excluirId} es la propia solicitud al modificarla.
     */
    public boolean existsAbiertaByCertificado(Long certificadoId, Long excluirId) {
        if (excluirId == null) {
            return count("certificadoId = ?1 and estado in ('REG', 'PND')", certificadoId) > 0;
        }
        return count("certificadoId = ?1 and estado in ('REG', 'PND') and id <> ?2", certificadoId, excluirId) > 0;
    }
}
