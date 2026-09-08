package com.bien.dominio.dto;

import com.bien.dominio.entity.Dominio;
import com.bien.dominio.entity.DominioValor;

import java.util.List;

public record DominioResponse(
        String id,
        String descripcion,
        String estado,
        List<Valor> valores) {

    public record Valor(String valor, String descripcion, String estado) {
        public static Valor from(DominioValor v) {
            return new Valor(v.getValor(), v.getDescripcionValor(), v.getEstado());
        }
    }

    public static DominioResponse from(Dominio d, List<DominioValor> valores) {
        return new DominioResponse(
                d.getId(),
                d.getDescripcion(),
                d.getEstado(),
                valores.stream().map(Valor::from).toList());
    }
}
