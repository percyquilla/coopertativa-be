package com.contabilidad.comprobante.repository;

import com.contabilidad.comprobante.entity.Comprobante;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class ComprobanteRepository implements PanacheRepositoryBase<Comprobante, Long> {

    /**
     * Listado con filtros opcionales. Se arma la cláusula a mano porque Panache
     * no tiene criteria dinámica y encadenar cuatro variantes fijas sería peor.
     */
    public List<Comprobante> listFiltrado(Long periodoId, Long tipoComprobanteId, String estado,
                                          LocalDate desde, LocalDate hasta) {
        List<String> condiciones = new ArrayList<>();
        Parameters params = new Parameters();

        if (periodoId != null) {
            condiciones.add("periodoId = :periodoId");
            params.and("periodoId", periodoId);
        }
        if (tipoComprobanteId != null) {
            condiciones.add("tipoComprobanteId = :tipoComprobanteId");
            params.and("tipoComprobanteId", tipoComprobanteId);
        }
        if (estado != null && !estado.isBlank()) {
            condiciones.add("estado = :estado");
            params.and("estado", estado);
        }
        if (desde != null) {
            condiciones.add("fecha >= :desde");
            params.and("desde", desde.atStartOfDay());
        }
        if (hasta != null) {
            condiciones.add("fecha < :hasta");
            params.and("hasta", hasta.plusDays(1).atStartOfDay());
        }

        String orden = "order by fecha desc, id desc";
        if (condiciones.isEmpty()) {
            return list(orden);
        }
        return list(String.join(" and ", condiciones) + " " + orden, params);
    }

    public List<Comprobante> listByPeriodo(Long periodoId) {
        return list("periodoId = ?1 order by fecha, id", periodoId);
    }

    public List<Comprobante> listByIds(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : list("id in ?1", ids);
    }

    public java.util.Optional<Comprobante> findByOperacion(Long operacionId) {
        return find("operacionId = ?1 order by id", operacionId).firstResultOptional();
    }

    public boolean existsBorradorEnPeriodo(Long periodoId) {
        return count("periodoId = ?1 and estado = 'REG'", periodoId) > 0;
    }

    public long countByEstadoYDia(String estado, LocalDate dia) {
        return count("estado = ?1 and fecha >= ?2 and fecha < ?3",
                estado, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }

    /**
     * Correlativo siguiente para el par (tipo, período). Delega en la función
     * de V7, que hace el upsert sobre {@code comprobante_secuencia} en una sola
     * sentencia y por eso es segura si dos cajeros autorizan a la vez.
     */
    public Long siguienteNumero(Long tipoComprobanteId, Long periodoId) {
        return (Long) getEntityManager()
                .createNativeQuery("select sc_contabilidad.fn_siguiente_numero_comprobante(:tipo, :periodo)")
                .setParameter("tipo", tipoComprobanteId)
                .setParameter("periodo", periodoId)
                .getSingleResult();
    }
}
