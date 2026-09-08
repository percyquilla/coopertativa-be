package com.bien.usuario.repository;

import com.bien.usuario.entity.UsuarioPwdHistorico;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UsuarioPwdHistoricoRepository implements PanacheRepositoryBase<UsuarioPwdHistorico, Long> {

    public List<UsuarioPwdHistorico> listUltimos(Long usuarioId, int limite) {
        return find("usuarioId = ?1 order by fechaHora desc", usuarioId)
                .page(0, limite)
                .list();
    }
}
