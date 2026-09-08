package com.contabilidad.comprobante.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Cabecera del asiento contable.
 * <p>
 * Ciclo de vida: <b>REG</b> (borrador, editable) → <b>AUT</b> (autorizado, con
 * número correlativo e inmutable) → nunca vuelve atrás. Un REG se puede anular
 * (<b>ANL</b>); un AUT solo se corrige emitiendo un comprobante de reversión,
 * enlazado por {@code comprobanteReversionId}.
 * <p>
 * El paso a AUT dispara {@code tr_comprobante_partida_doble}, que rechaza el
 * asiento si no tiene renglones, si no cuadra o si suma cero.
 */
@Entity
@Table(name = "comprobante", schema = "sc_contabilidad")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comprobante_id")
    private Long id;

    @Column(name = "periodo_id", nullable = false)
    private Long periodoId;

    @Column(name = "operacion_id")
    private Long operacionId;

    @Column(name = "tipo_comprobante_id", nullable = false)
    private Long tipoComprobanteId;

    /** Correlativo por tipo y período. Null mientras el comprobante es borrador. */
    @Column(name = "numero")
    private Long numero;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "glosa", length = 300)
    private String glosa;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    @Column(name = "usuario_creacion", length = 100)
    private String usuarioCreacion;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_autoriz", length = 100)
    private String usuarioAutoriz;

    @Column(name = "fecha_autoriz")
    private LocalDateTime fechaAutoriz;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    /** Comprobante que revierte a este. Se llena al anular uno ya autorizado. */
    @Column(name = "comprobante_reversion_id")
    private Long comprobanteReversionId;

    public boolean esBorrador() {
        return "REG".equals(estado);
    }

    public boolean estaAutorizado() {
        return "AUT".equals(estado);
    }

    public Long getId() {
        return id;
    }

    public Long getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(Long periodoId) {
        this.periodoId = periodoId;
    }

    public Long getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(Long operacionId) {
        this.operacionId = operacionId;
    }

    public Long getTipoComprobanteId() {
        return tipoComprobanteId;
    }

    public void setTipoComprobanteId(Long tipoComprobanteId) {
        this.tipoComprobanteId = tipoComprobanteId;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getUsuarioAutoriz() {
        return usuarioAutoriz;
    }

    public void setUsuarioAutoriz(String usuarioAutoriz) {
        this.usuarioAutoriz = usuarioAutoriz;
    }

    public LocalDateTime getFechaAutoriz() {
        return fechaAutoriz;
    }

    public void setFechaAutoriz(LocalDateTime fechaAutoriz) {
        this.fechaAutoriz = fechaAutoriz;
    }

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }

    public Long getComprobanteReversionId() {
        return comprobanteReversionId;
    }

    public void setComprobanteReversionId(Long comprobanteReversionId) {
        this.comprobanteReversionId = comprobanteReversionId;
    }
}
