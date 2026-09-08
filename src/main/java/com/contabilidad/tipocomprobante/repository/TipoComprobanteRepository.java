package com.contabilidad.tipocomprobante.repository;

import com.contabilidad.tipocomprobante.entity.TipoComprobante;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TipoComprobanteRepository implements PanacheRepositoryBase<TipoComprobante, Long> {

    public List<TipoComprobante> listOrdenados() {
        return list("order by codigo");
    }

    public Optional<TipoComprobante> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }
}
