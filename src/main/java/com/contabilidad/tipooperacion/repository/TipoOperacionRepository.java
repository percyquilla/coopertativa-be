package com.contabilidad.tipooperacion.repository;

import com.contabilidad.tipooperacion.entity.TipoOperacion;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TipoOperacionRepository implements PanacheRepositoryBase<TipoOperacion, Long> {

    public List<TipoOperacion> listOrdenados() {
        return list("order by codigo");
    }

    public Optional<TipoOperacion> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }

    public boolean existsByCodigo(String codigo) {
        return count("codigo", codigo) > 0;
    }
}
