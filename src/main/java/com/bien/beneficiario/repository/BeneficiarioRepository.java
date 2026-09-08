package com.bien.beneficiario.repository;

import com.bien.beneficiario.entity.Beneficiario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class BeneficiarioRepository implements PanacheRepositoryBase<Beneficiario, Long> {

    public List<Beneficiario> listBySocio(Long socioId) {
        return list("socioId = ?1 order by estado, nombreCompleto", socioId);
    }

    /** Suma de porcentajes vigentes, excluyendo uno (para validar una edición). */
    public BigDecimal sumaVigente(Long socioId, Long excluirId) {
        BigDecimal suma = getEntityManager()
                .createQuery("select coalesce(sum(b.porcentaje), 0) from Beneficiario b "
                        + "where b.socioId = :s and b.estado = 'V' and b.id <> :e", BigDecimal.class)
                .setParameter("s", socioId)
                .setParameter("e", excluirId == null ? -1L : excluirId)
                .getSingleResult();
        return suma == null ? BigDecimal.ZERO : suma;
    }
}
