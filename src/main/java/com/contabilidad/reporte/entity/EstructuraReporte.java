package com.contabilidad.reporte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Cabecera de un reporte configurable (ESF, ER). Se siembra en V7. */
@Entity
@Table(name = "estructura_reporte", schema = "sc_contabilidad")
public class EstructuraReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estructura_reporte_id")
    private Long id;

    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }
}
