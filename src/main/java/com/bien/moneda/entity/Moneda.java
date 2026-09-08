package com.bien.moneda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "moneda", schema = "sc_administracion")
public class Moneda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moneda_id")
    private Long id;

    @Column(name = "codigo", length = 10, nullable = false)
    private String codigo;

    // La columna del diseño base está escrita "desripcion".
    @Column(name = "desripcion", length = 100, nullable = false)
    private String descripcion;

    @Column(name = "simbolo", length = 10)
    private String simbolo;

    @Column(name = "estado", length = 2, nullable = false)
    private String estado;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
