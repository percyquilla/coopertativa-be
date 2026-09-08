package com.contabilidad.diacontable.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Estado contable de una fecha. Funciona igual que {@code PeriodoContable}:
 * estados 'P' pendiente de apertura · 'A' abierto · 'C' cerrado · 'R' reabierto.
 *
 * <p>Toda fecha dentro de un período no cerrado tiene su fila (las genera
 * {@code PeriodoContableService.generarGestion}). Una fecha sin fila NO se puede
 * asentar, igual que no se puede asentar en una fecha sin período. Solo puede
 * haber una fila en 'A' o 'R' a la vez, y los días se abren y cierran en orden.
 */
@Entity
@Table(name = "dia_contable", schema = "sc_contabilidad")
public class DiaContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dia_id")
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "periodo_id")
    private Long periodoId;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    /** Se puede asentar en el día: abierto o reabierto. */
    public boolean estaAbierto() {
        return "A".equals(estado) || "R".equals(estado);
    }

    public boolean estaCerrado() {
        return "C".equals(estado);
    }

    public boolean estaPendiente() {
        return "P".equals(estado);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(Long periodoId) {
        this.periodoId = periodoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
