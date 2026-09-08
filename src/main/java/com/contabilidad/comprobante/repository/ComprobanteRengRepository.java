package com.contabilidad.comprobante.repository;

import com.contabilidad.comprobante.entity.ComprobanteReng;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ComprobanteRengRepository implements PanacheRepositoryBase<ComprobanteReng, Long> {

    public List<ComprobanteReng> listByComprobante(Long comprobanteId) {
        return list("comprobanteId = ?1 order by secuencia", comprobanteId);
    }

    public long deleteByComprobante(Long comprobanteId) {
        return delete("comprobanteId", comprobanteId);
    }

    /** Renglones de varios comprobantes de una, para no consultar uno por fila. */
    public Map<Long, List<ComprobanteReng>> agrupadosPorComprobante(List<Long> comprobanteIds) {
        if (comprobanteIds.isEmpty()) {
            return Map.of();
        }
        return list("comprobanteId in ?1 order by secuencia", comprobanteIds).stream()
                .collect(Collectors.groupingBy(ComprobanteReng::getComprobanteId));
    }

    /** Renglones de una cuenta afectable en un período, solo de comprobantes autorizados. */
    public List<ComprobanteReng> listAutorizadosDeAfectable(Long periodoId, Long afectableId) {
        return getEntityManager()
                .createQuery("select r from ComprobanteReng r, Comprobante c "
                        + "where r.comprobanteId = c.id and c.periodoId = :periodoId "
                        + "and r.afectableId = :afectableId and c.estado = 'AUT' "
                        + "order by c.fecha, c.id, r.secuencia", ComprobanteReng.class)
                .setParameter("periodoId", periodoId)
                .setParameter("afectableId", afectableId)
                .getResultList();
    }
}
