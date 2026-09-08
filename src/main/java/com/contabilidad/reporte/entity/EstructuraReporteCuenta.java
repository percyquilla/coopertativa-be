package com.contabilidad.reporte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Mapeo de una línea DETALLE a una cuenta contable. {@code cuentaId} apunta a
 * {@code cuenta} (no a la afectable): si es agrupadora, arrastra todas sus
 * cuentas de mayor descendientes. {@code signo} permite restar (p. ej. gastos
 * dentro del resultado del ejercicio).
 */
@Entity
@Table(name = "estructura_reporte_cuenta", schema = "sc_contabilidad")
public class EstructuraReporteCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estructura_reporte_cuenta_id")
    private Long id;

    @Column(name = "estructura_reporte_linea_id", nullable = false)
    private Long estructuraReporteLineaId;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "signo", nullable = false)
    private Short signo;

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "estacion", length = 30, nullable = false)
    private String estacion;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    public Long getId() {
        return id;
    }

    public Long getEstructuraReporteLineaId() {
        return estructuraReporteLineaId;
    }

    public void setEstructuraReporteLineaId(Long estructuraReporteLineaId) {
        this.estructuraReporteLineaId = estructuraReporteLineaId;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public Short getSigno() {
        return signo;
    }

    public void setSigno(Short signo) {
        this.signo = signo;
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
