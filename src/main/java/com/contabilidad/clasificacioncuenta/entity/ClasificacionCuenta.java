package com.contabilidad.clasificacioncuenta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Clasificación de una cuenta contable (ACT/PAS/PAT/ING/GAS) con su naturaleza
 * deudora o acreedora. Catálogo de solo lectura, sembrado por V10.
 */
@Entity
@Table(name = "clasificacion_cuenta", schema = "sc_contabilidad")
public class ClasificacionCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clasificacion_id")
    private Long id;

    @Column(name = "codigo", length = 20, nullable = false)
    private String codigo;

    /** Naturaleza: 'D' deudora, 'H' acreedora (dom_naturaleza). */
    @Column(name = "dom_debe_haber", length = 2, nullable = false)
    private String naturaleza;

    @Column(name = "descripcion", length = 100, nullable = false)
    private String descripcion;

    @Column(name = "estado", length = 2, nullable = false)
    private String estado;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNaturaleza() {
        return naturaleza;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }
}
