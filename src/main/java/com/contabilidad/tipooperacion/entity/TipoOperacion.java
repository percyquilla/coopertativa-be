package com.contabilidad.tipooperacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase de operación de negocio que dispara un asiento automático (depósito,
 * retiro, emisión de aporte…). Cada una apunta a su {@code esquema_contable}
 * (la receta) y al {@code tipo_comprobante} que emite.
 */
@Entity
@Table(name = "tipo_operacion", schema = "sc_administracion")
public class TipoOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_operacion_id")
    private Long id;

    @Column(name = "codigo", length = 20, nullable = false)
    private String codigo;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @Column(name = "estado", length = 15, nullable = false)
    private String estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "esquema_contable_id")
    private Long esquemaContableId;

    @Column(name = "tipo_comprobante_id")
    private Long tipoComprobanteId;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public boolean estaVigente(LocalDate fecha) {
        return "V".equals(estado)
                && (fechaInicio == null || !fechaInicio.isAfter(fecha))
                && (fechaFin == null || !fechaFin.isBefore(fecha));
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Long getEsquemaContableId() {
        return esquemaContableId;
    }

    public void setEsquemaContableId(Long esquemaContableId) {
        this.esquemaContableId = esquemaContableId;
    }

    public Long getTipoComprobanteId() {
        return tipoComprobanteId;
    }

    public void setTipoComprobanteId(Long tipoComprobanteId) {
        this.tipoComprobanteId = tipoComprobanteId;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }
}
