package com.bien.socio.repository;

import com.bien.socio.dto.SocioReporteRow;
import com.bien.socio.entity.Socio;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SocioRepository implements PanacheRepositoryBase<Socio, Long> {

    public boolean existsByPersonaId(Long personaId) {
        return count("personaId", personaId) > 0;
    }

    public boolean existsByNumeroSocio(String numeroSocio) {
        return count("numeroSocio", numeroSocio) > 0;
    }

    public boolean existsByNumeroSocioAndIdNot(String numeroSocio, Long id) {
        return count("numeroSocio = ?1 and id <> ?2", numeroSocio, id) > 0;
    }

    /**
     * Reporte de socios (Obs. #5) en una sola consulta: datos de persona,
     * dirección (la principal vigente, o la primera vigente) y suma nominal de
     * los certificados vigentes.
     */
    @SuppressWarnings("unchecked")
    public List<SocioReporteRow> reporte() {
        List<Object[]> rows = getEntityManager().createNativeQuery("""
                SELECT s.socio_id, s.numero_socio, p.nombres, p.apellido_paterno, p.apellido_materno,
                       p.apellido_casada, p.cedula_identidad, p.expedicion, p.numero_telefono,
                       COALESCE(p.correo_personal, p.correo_institucional), d.direccion, p.genero,
                       s.fecha_ingreso, COALESCE(c.monto, 0), s.estado
                  FROM sc_socios.socio s
                  JOIN sc_socios.persona p ON p.persona_id = s.persona_id
                  LEFT JOIN LATERAL (
                        SELECT pd.direccion
                          FROM sc_socios.persona_direccion pd
                         WHERE pd.persona_id = p.persona_id AND pd.estado = 'V'
                         ORDER BY pd.es_principal DESC, pd.persona_direccion_id
                         LIMIT 1) d ON TRUE
                  LEFT JOIN (
                        SELECT ca.socio_id, SUM(ca.valor_unitario * ca.cantidad) AS monto
                          FROM sc_socios.certificado_aportacion ca
                         WHERE ca.estado = 'VIG'
                         GROUP BY ca.socio_id) c ON c.socio_id = s.socio_id
                 ORDER BY s.numero_socio
                """).getResultList();
        return rows.stream().map(r -> new SocioReporteRow(
                ((Number) r[0]).longValue(),
                (String) r[1], (String) r[2], (String) r[3], (String) r[4], (String) r[5],
                (String) r[6], (String) r[7], (String) r[8], (String) r[9], (String) r[10], (String) r[11],
                toLocalDate(r[12]),
                r[13] == null ? BigDecimal.ZERO : new BigDecimal(r[13].toString()),
                (String) r[14])).toList();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate d) {
            return d;
        }
        return ((Date) value).toLocalDate();
    }
}
