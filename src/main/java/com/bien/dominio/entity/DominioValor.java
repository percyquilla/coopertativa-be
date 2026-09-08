package com.bien.dominio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "dominio_valor", schema = "sc_administracion")
public class DominioValor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dominio_valor_id")
    private Long id;

    @Column(name = "dominio_id", length = 30, nullable = false)
    private String dominioId;

    @Column(name = "valor", length = 20, nullable = false)
    private String valor;

    @Column(name = "descripcion_valor", length = 100, nullable = false)
    private String descripcionValor;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    @Column(name = "cod_usuario", length = 50, nullable = false)
    private String codUsuario;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public Long getId() {
        return id;
    }

    public String getDominioId() {
        return dominioId;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcionValor() {
        return descripcionValor;
    }

    public String getEstado() {
        return estado;
    }

    public String getCodUsuario() {
        return codUsuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getEstacion() {
        return estacion;
    }
}
