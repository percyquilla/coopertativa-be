package com.bien.certificadoaportacion.repository;

import com.bien.certificadoaportacion.entity.CertificadoAportacion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CertificadoAportacionRepository implements PanacheRepositoryBase<CertificadoAportacion, Long> {

    /** Certificado autorizado y en vigor. */
    public static final String VIGENTE = "VIG";

    /** Eliminación lógica: no se lista. */
    public static final String ELIMINADO = "ELM";

    public boolean existsByNumero(String numero) {
        return count("numero", numero) > 0;
    }

    public List<CertificadoAportacion> listNoEliminados() {
        return list("estado <> ?1 order by fechaEmision desc, id desc", ELIMINADO);
    }

    public List<CertificadoAportacion> listBySocio(Long socioId) {
        return list("socioId = ?1 and estado <> ?2 order by fechaEmision desc, id desc", socioId, ELIMINADO);
    }

    public List<CertificadoAportacion> listVigentesBySocio(Long socioId) {
        return list("socioId = ?1 and estado = ?2 order by fechaEmision, id", socioId, VIGENTE);
    }

    public long countVigentesBySocio(Long socioId) {
        return count("socioId = ?1 and estado = ?2", socioId, VIGENTE);
    }

    /**
     * Mayor número de certificado tomado como entero (Obs. #7). Los números no
     * numéricos de cargas antiguas se ignoran.
     */
    public long maxNumero() {
        Object max = getEntityManager().createNativeQuery(
                        "SELECT COALESCE(MAX(CAST(numero AS BIGINT)), 0) "
                                + "FROM sc_socios.certificado_aportacion WHERE numero ~ '^[0-9]{1,18}$'")
                .getSingleResult();
        return ((Number) max).longValue();
    }

    /**
     * Serializa la asignación de números entre transacciones concurrentes hasta
     * el commit, para que dos emisiones simultáneas no tomen el mismo número.
     */
    public void bloquearNumeracion() {
        getEntityManager().createNativeQuery(
                        // CAST: la función devuelve void, que Hibernate no sabe mapear
                        "SELECT CAST(pg_advisory_xact_lock(hashtext('sc_socios.certificado_aportacion.numero')) AS TEXT)")
                .getSingleResult();
    }

    /**
     * Cuenta CA del socio (modelo anterior, en el que cada certificado se
     * apoyaba en una cuenta de producto CA). Los certificados nuevos ya no
     * tienen cuenta; se conserva para los datos existentes.
     */
    public Optional<Long> findCuentaCaBySocio(Long socioId) {
        return find("socioId = ?1 and cuentaCaptacionId is not null order by id", socioId)
                .firstResultOptional()
                .map(CertificadoAportacion::getCuentaCaptacionId);
    }
}
