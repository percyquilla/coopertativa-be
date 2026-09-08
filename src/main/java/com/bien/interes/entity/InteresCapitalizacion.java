package com.bien.interes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Capitalización mensual del interés devengado: suma {@code montoInteres} al
 * saldo de la cuenta. Una fila por {@link InteresCaptacion} (relación 1:1).
 */
@Entity
@Table(name = "interes_capitalizacion", schema = "sc_captaciones")
public class InteresCapitalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interes_capitalizacion_id")
    private Long id;

    @Column(name = "interes_captacion_id", nullable = false)
    private Long interesCaptacionId;

    @Column(name = "cuenta_captacion_id", nullable = false)
    private Long cuentaCaptacionId;

    @Column(name = "fecha_capitalizacion", insertable = false, updatable = false)
    private LocalDateTime fechaCapitalizacion;

    @Column(name = "monto_interes", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoInteres;

    @Column(name = "saldo_anterior", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "usuario", length = 50)
    private String usuario;

    public Long getId() {
        return id;
    }

    public Long getInteresCaptacionId() {
        return interesCaptacionId;
    }

    public void setInteresCaptacionId(Long interesCaptacionId) {
        this.interesCaptacionId = interesCaptacionId;
    }

    public Long getCuentaCaptacionId() {
        return cuentaCaptacionId;
    }

    public void setCuentaCaptacionId(Long cuentaCaptacionId) {
        this.cuentaCaptacionId = cuentaCaptacionId;
    }

    public LocalDateTime getFechaCapitalizacion() {
        return fechaCapitalizacion;
    }

    public BigDecimal getMontoInteres() {
        return montoInteres;
    }

    public void setMontoInteres(BigDecimal montoInteres) {
        this.montoInteres = montoInteres;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public void setSaldoAnterior(BigDecimal saldoAnterior) {
        this.saldoAnterior = saldoAnterior;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public void setSaldoPosterior(BigDecimal saldoPosterior) {
        this.saldoPosterior = saldoPosterior;
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
}
