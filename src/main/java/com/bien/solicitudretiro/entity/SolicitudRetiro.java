package com.bien.solicitudretiro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Solicitud de retiro del socio (RF-SOC-007). Es la única vía por la que se
 * devuelven las aportaciones: al pagarla se vacía la cuenta CA del socio.
 */
@Entity
@Table(name = "solicitud_retiro", schema = "sc_socios")
public class SolicitudRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitud_retiro_id")
    private Long id;

    @Column(name = "socio_id", nullable = false)
    private Long socioId;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "estado", length = 15, nullable = false)
    private String estado;

    @Column(name = "monto_aportes", precision = 18, scale = 2)
    private BigDecimal montoAportes;

    @Column(name = "fecha_resolucion")
    private LocalDate fechaResolucion;

    @Column(name = "usuario_resolucion", length = 50)
    private String usuarioResolucion;

    @Column(name = "observacion_resolucion", length = 300)
    private String observacionResolucion;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public Long getId() {
        return id;
    }

    public Long getSocioId() {
        return socioId;
    }

    public void setSocioId(Long socioId) {
        this.socioId = socioId;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoAportes() {
        return montoAportes;
    }

    public void setMontoAportes(BigDecimal montoAportes) {
        this.montoAportes = montoAportes;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDate fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getUsuarioResolucion() {
        return usuarioResolucion;
    }

    public void setUsuarioResolucion(String usuarioResolucion) {
        this.usuarioResolucion = usuarioResolucion;
    }

    public String getObservacionResolucion() {
        return observacionResolucion;
    }

    public void setObservacionResolucion(String observacionResolucion) {
        this.observacionResolucion = observacionResolucion;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
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
