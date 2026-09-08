package com.contabilidad.saldo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Saldo acumulado de una cuenta afectable dentro de un período. Una fila por
 * par {@code (periodo, afectable)}: el {@code saldo_inicial} viene del período
 * anterior, y {@code total_debito}/{@code total_credito} suman los renglones de
 * los comprobantes autorizados de este período.
 * <p>
 * {@code saldo_final} depende de la naturaleza de la cuenta: en las deudoras
 * (activo, gasto) los débitos suman y los créditos restan; en las acreedoras
 * (pasivo, patrimonio, ingreso) es al revés. Ese cálculo lo hace el servicio,
 * no la base.
 */
@Entity
@Table(name = "saldo_cuenta", schema = "sc_contabilidad")
public class SaldoCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saldo_id")
    private Long id;

    @Column(name = "periodo_id", nullable = false)
    private Long periodoId;

    @Column(name = "afectable_id", nullable = false)
    private Long afectableId;

    @Column(name = "saldo_inicial", nullable = false)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Column(name = "total_debito", nullable = false)
    private BigDecimal totalDebito = BigDecimal.ZERO;

    @Column(name = "total_credito", nullable = false)
    private BigDecimal totalCredito = BigDecimal.ZERO;

    @Column(name = "saldo_final", nullable = false)
    private BigDecimal saldoFinal = BigDecimal.ZERO;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public Long getId() {
        return id;
    }

    public Long getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(Long periodoId) {
        this.periodoId = periodoId;
    }

    public Long getAfectableId() {
        return afectableId;
    }

    public void setAfectableId(Long afectableId) {
        this.afectableId = afectableId;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public BigDecimal getTotalDebito() {
        return totalDebito;
    }

    public void setTotalDebito(BigDecimal totalDebito) {
        this.totalDebito = totalDebito;
    }

    public BigDecimal getTotalCredito() {
        return totalCredito;
    }

    public void setTotalCredito(BigDecimal totalCredito) {
        this.totalCredito = totalCredito;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(BigDecimal saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
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
}
