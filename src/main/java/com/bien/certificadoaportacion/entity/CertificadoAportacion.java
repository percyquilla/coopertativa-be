package com.bien.certificadoaportacion.entity;

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
 * Título nominal de aportación (§5.1). La mecánica de saldo/movimientos la lleva
 * la {@code cuenta_captacion} de producto tipo CA a la que apunta este título.
 */
@Entity
@Table(name = "certificado_aportacion", schema = "sc_socios")
public class CertificadoAportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificado_id")
    private Long id;

    @Column(name = "socio_id", nullable = false)
    private Long socioId;

    @Column(name = "cuenta_captacion_id")
    private Long cuentaCaptacionId;

    @Column(name = "numero", length = 30, nullable = false)
    private String numero;

    @Column(name = "valor_unitario", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "estado_verif", length = 15, nullable = false)
    private String estadoVerif;

    @Column(name = "estado", length = 15, nullable = false)
    private String estado;

    @Column(name = "usuario", length = 50)
    private String usuario;

    /** Lo pone la BD al insertar; el servicio lo vuelve a sellar en cada cambio de estado. */
    @Column(name = "fecha_hora", insertable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    /** Operación contable generada al autorizar la emisión (APO_EMI). */
    @Column(name = "operacion_id")
    private Long operacionId;

    public Long getId() {
        return id;
    }

    public Long getSocioId() {
        return socioId;
    }

    public void setSocioId(Long socioId) {
        this.socioId = socioId;
    }

    public Long getCuentaCaptacionId() {
        return cuentaCaptacionId;
    }

    public void setCuentaCaptacionId(Long cuentaCaptacionId) {
        this.cuentaCaptacionId = cuentaCaptacionId;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstadoVerif() {
        return estadoVerif;
    }

    public void setEstadoVerif(String estadoVerif) {
        this.estadoVerif = estadoVerif;
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Long getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(Long operacionId) {
        this.operacionId = operacionId;
    }

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }
}
