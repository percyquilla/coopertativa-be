package com.bien.certificadoaportacion.repository;

import com.bien.certificadoaportacion.entity.CertificadoAportacion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CertificadoAportacionRepository implements PanacheRepositoryBase<CertificadoAportacion, Long> {

    public boolean existsByNumero(String numero) {
        return count("numero", numero) > 0;
    }

    public List<CertificadoAportacion> listBySocio(Long socioId) {
        return list("socioId = ?1 order by fechaEmision desc, id desc", socioId);
    }

    /**
     * Cuenta CA del socio: la lleva el primer certificado vigente que la tenga
     * asignada. Todos los certificados de un socio comparten la misma cuenta.
     */
    public Optional<Long> findCuentaCaBySocio(Long socioId) {
        return find("socioId = ?1 and cuentaCaptacionId is not null order by id", socioId)
                .firstResultOptional()
                .map(CertificadoAportacion::getCuentaCaptacionId);
    }
}
