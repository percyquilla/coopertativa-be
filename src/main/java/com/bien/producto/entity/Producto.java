package com.bien.producto.entity;

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
@Table(name = "producto", schema = "sc_administracion")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long id;

    @Column(name = "codigo", length = 20, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @Column(name = "tipo_producto", length = 20, nullable = false)
    private String tipoProducto;

    @Column(name = "moneda", length = 10, nullable = false)
    private String moneda;

    @Column(name = "saldo_minimo", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoMinimo;

    @Column(name = "permite_deposito", nullable = false)
    private boolean permiteDeposito;

    @Column(name = "permite_retiro", nullable = false)
    private boolean permiteRetiro;

    @Column(name = "permite_interes", nullable = false)
    private boolean permiteInteres;

    @Column(name = "estado", length = 15, nullable = false)
    private String estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    /**
     * Un producto se ofrece solo si está vigente <em>y</em> la fecha cae dentro de
     * su vigencia. El estado por sí solo no alcanza: `fecha_fin` existe para
     * discontinuar un producto sin tener que acordarse de suspenderlo a mano.
     */
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getSaldoMinimo() {
        return saldoMinimo;
    }

    public void setSaldoMinimo(BigDecimal saldoMinimo) {
        this.saldoMinimo = saldoMinimo;
    }

    public boolean isPermiteDeposito() {
        return permiteDeposito;
    }

    public void setPermiteDeposito(boolean permiteDeposito) {
        this.permiteDeposito = permiteDeposito;
    }

    public boolean isPermiteRetiro() {
        return permiteRetiro;
    }

    public void setPermiteRetiro(boolean permiteRetiro) {
        this.permiteRetiro = permiteRetiro;
    }

    public boolean isPermiteInteres() {
        return permiteInteres;
    }

    public void setPermiteInteres(boolean permiteInteres) {
        this.permiteInteres = permiteInteres;
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
