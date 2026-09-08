package com.contabilidad.reporte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Una fila del reporte. {@code tipoLinea}: TITULO (encabezado, sin importe),
 * DETALLE (suma cuentas mapeadas), SUBTOTAL / TOTAL (evalúa {@code formula}
 * sobre los códigos de otras líneas). {@code codigo} es único dentro del reporte
 * y se usa como variable en las fórmulas de las demás líneas.
 */
@Entity
@Table(name = "estructura_reporte_linea", schema = "sc_contabilidad")
public class EstructuraReporteLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estructura_reporte_linea_id")
    private Long id;

    @Column(name = "estructura_reporte_id", nullable = false)
    private Long estructuraReporteId;

    @Column(name = "linea_padre_id")
    private Long lineaPadreId;

    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    @Column(name = "titulo", length = 200, nullable = false)
    private String titulo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "tipo_linea", length = 20, nullable = false)
    private String tipoLinea;

    @Column(name = "signo", nullable = false)
    private Short signo;

    @Column(name = "formula", length = 500)
    private String formula;

    @Column(name = "estado", length = 3, nullable = false)
    private String estado;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    public Long getId() {
        return id;
    }

    public Long getEstructuraReporteId() {
        return estructuraReporteId;
    }

    public void setEstructuraReporteId(Long estructuraReporteId) {
        this.estructuraReporteId = estructuraReporteId;
    }

    public Long getLineaPadreId() {
        return lineaPadreId;
    }

    public void setLineaPadreId(Long lineaPadreId) {
        this.lineaPadreId = lineaPadreId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getTipoLinea() {
        return tipoLinea;
    }

    public void setTipoLinea(String tipoLinea) {
        this.tipoLinea = tipoLinea;
    }

    public Short getSigno() {
        return signo;
    }

    public void setSigno(Short signo) {
        this.signo = signo;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
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
