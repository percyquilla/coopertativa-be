package com.bien.dominio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Catálogo de códigos del sistema (tipo de producto, estado de cuenta, tipo de
 * movimiento, …). Es documentación viva: las validaciones y los desplegables
 * siguen definidos en el código, este catálogo dice qué significa cada código.
 */
@Entity
@Table(name = "dominio", schema = "sc_administracion")
public class Dominio {

    @Id
    @Column(name = "dominio_id", length = 30)
    private String id;

    @Column(name = "descripcion", length = 128, nullable = false)
    private String descripcion;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    @Column(name = "usuario", length = 50, nullable = false)
    private String usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getEstacion() {
        return estacion;
    }
}
