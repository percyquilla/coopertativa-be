package com.contabilidad.operacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un hecho de negocio que debe reflejarse en contabilidad (un depósito, un pago
 * de retiro, una emisión de aporte). El motor la crea, le aplica su esquema y
 * genera el comprobante.
 * <p>
 * Estado: <b>PROC</b> contabilizada · <b>PND</b> el asiento falló y quedó en la
 * cola para reprocesar (con {@code errorMensaje}). Nunca queda en 'REG': o se
 * contabiliza en el acto o entra como PND.
 */
@Entity
@Table(name = "operacion_contable", schema = "sc_contabilidad")
public class OperacionContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operacion_id")
    private Long id;

    @Column(name = "tipo_operacion_id", nullable = false)
    private Long tipoOperacionId;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;

    @Column(name = "glosa", length = 200, nullable = false)
    private String glosa;

    @Column(name = "moneda_id", nullable = false)
    private Long monedaId;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    @Column(name = "esquema_contable_id")
    private Long esquemaContableId;

    @Column(name = "origen_modulo", length = 30)
    private String origenModulo;

    @Column(name = "origen_tabla", length = 60)
    private String origenTabla;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "operacion_reversion_id")
    private Long operacionReversionId;

    @Column(name = "error_mensaje", length = 500)
    private String errorMensaje;

    @Column(name = "fecha_proceso")
    private LocalDateTime fechaProceso;

    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    public boolean estaPendiente() {
        return "PND".equals(estado);
    }

    public Long getId() {
        return id;
    }

    public Long getTipoOperacionId() {
        return tipoOperacionId;
    }

    public void setTipoOperacionId(Long tipoOperacionId) {
        this.tipoOperacionId = tipoOperacionId;
    }

    public LocalDateTime getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(LocalDateTime fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

    public Long getMonedaId() {
        return monedaId;
    }

    public void setMonedaId(Long monedaId) {
        this.monedaId = monedaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getEsquemaContableId() {
        return esquemaContableId;
    }

    public void setEsquemaContableId(Long esquemaContableId) {
        this.esquemaContableId = esquemaContableId;
    }

    public String getOrigenModulo() {
        return origenModulo;
    }

    public void setOrigenModulo(String origenModulo) {
        this.origenModulo = origenModulo;
    }

    public String getOrigenTabla() {
        return origenTabla;
    }

    public void setOrigenTabla(String origenTabla) {
        this.origenTabla = origenTabla;
    }

    public Long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Long origenId) {
        this.origenId = origenId;
    }

    public Long getOperacionReversionId() {
        return operacionReversionId;
    }

    public void setOperacionReversionId(Long operacionReversionId) {
        this.operacionReversionId = operacionReversionId;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }

    public void setErrorMensaje(String errorMensaje) {
        this.errorMensaje = errorMensaje;
    }

    public LocalDateTime getFechaProceso() {
        return fechaProceso;
    }

    public void setFechaProceso(LocalDateTime fechaProceso) {
        this.fechaProceso = fechaProceso;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
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
