package com.bien.interes.entity;

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
 * Devengo de intereses de una cuenta de captación en un mes. Hay una fila por
 * cuenta y mes: cada cierre de día suma el interés de ese día a
 * {@code interesDevengado} y avanza {@code dias} y {@code fechaFinPeriodo}.
 * Estados: 'REG' acumulando · 'CAP' ya capitalizada · 'ANL' anulada.
 */
@Entity
@Table(name = "interes_captacion", schema = "sc_captaciones")
public class InteresCaptacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interes_captacion_id")
    private Long id;

    @Column(name = "cuenta_captacion_id", nullable = false)
    private Long cuentaCaptacionId;

    @Column(name = "fecha_inicio_periodo", nullable = false)
    private LocalDate fechaInicioPeriodo;

    @Column(name = "fecha_fin_periodo", nullable = false)
    private LocalDate fechaFinPeriodo;

    @Column(name = "saldo_base", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoBase;

    @Column(name = "tasa", nullable = false, precision = 10, scale = 6)
    private BigDecimal tasa;

    @Column(name = "dias", nullable = false)
    private Integer dias;

    @Column(name = "interes_calculado", nullable = false, precision = 18, scale = 2)
    private BigDecimal interesCalculado;

    @Column(name = "interes_devengado", nullable = false, precision = 18, scale = 2)
    private BigDecimal interesDevengado;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "fecha_calculo", insertable = false, updatable = false)
    private LocalDateTime fechaCalculo;

    @Column(name = "usuario", length = 50)
    private String usuario;

    public Long getId() {
        return id;
    }

    public Long getCuentaCaptacionId() {
        return cuentaCaptacionId;
    }

    public void setCuentaCaptacionId(Long cuentaCaptacionId) {
        this.cuentaCaptacionId = cuentaCaptacionId;
    }

    public LocalDate getFechaInicioPeriodo() {
        return fechaInicioPeriodo;
    }

    public void setFechaInicioPeriodo(LocalDate fechaInicioPeriodo) {
        this.fechaInicioPeriodo = fechaInicioPeriodo;
    }

    public LocalDate getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(LocalDate fechaFinPeriodo) {
        this.fechaFinPeriodo = fechaFinPeriodo;
    }

    public BigDecimal getSaldoBase() {
        return saldoBase;
    }

    public void setSaldoBase(BigDecimal saldoBase) {
        this.saldoBase = saldoBase;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public void setTasa(BigDecimal tasa) {
        this.tasa = tasa;
    }

    public Integer getDias() {
        return dias;
    }

    public void setDias(Integer dias) {
        this.dias = dias;
    }

    public BigDecimal getInteresCalculado() {
        return interesCalculado;
    }

    public void setInteresCalculado(BigDecimal interesCalculado) {
        this.interesCalculado = interesCalculado;
    }

    public BigDecimal getInteresDevengado() {
        return interesDevengado;
    }

    public void setInteresDevengado(BigDecimal interesDevengado) {
        this.interesDevengado = interesDevengado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
