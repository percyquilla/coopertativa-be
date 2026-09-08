package com.bien.tipocambio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_cambio", schema = "sc_parametros")
public class TipoCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_cambio_id")
    private Long id;

    @Column(name = "moneda_id", nullable = false)
    private Long monedaId;

    @Column(name = "moneda_destino_id", nullable = false)
    private Long monedaDestinoId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "compra", nullable = false, precision = 20, scale = 10)
    private BigDecimal compra;

    @Column(name = "venta", nullable = false, precision = 20, scale = 10)
    private BigDecimal venta;

    @Column(name = "oficial", precision = 20, scale = 10)
    private BigDecimal oficial;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public Long getId() {
        return id;
    }

    public Long getMonedaId() {
        return monedaId;
    }

    public void setMonedaId(Long monedaId) {
        this.monedaId = monedaId;
    }

    public Long getMonedaDestinoId() {
        return monedaDestinoId;
    }

    public void setMonedaDestinoId(Long monedaDestinoId) {
        this.monedaDestinoId = monedaDestinoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCompra() {
        return compra;
    }

    public void setCompra(BigDecimal compra) {
        this.compra = compra;
    }

    public BigDecimal getVenta() {
        return venta;
    }

    public void setVenta(BigDecimal venta) {
        this.venta = venta;
    }

    public BigDecimal getOficial() {
        return oficial;
    }

    public void setOficial(BigDecimal oficial) {
        this.oficial = oficial;
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

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }
}
