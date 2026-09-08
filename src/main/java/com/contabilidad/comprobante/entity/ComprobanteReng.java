package com.contabilidad.comprobante.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Renglón del asiento: una cuenta afectable, un lado (D/H) y un importe.
 * <p>
 * {@code monto} está siempre en la moneda base de la institución — es lo que
 * suman los saldos y lo que cuadra el trigger de partida doble. {@code montoMo}
 * y {@code tipoCambio} guardan el importe en la moneda original cuando la
 * operación no fue en moneda base.
 */
@Entity
@Table(name = "comprobante_reng", schema = "sc_contabilidad")
public class ComprobanteReng {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comprobante_reng_id")
    private Long id;

    @Column(name = "comprobante_id", nullable = false)
    private Long comprobanteId;

    @Column(name = "secuencia", nullable = false)
    private Integer secuencia;

    @Column(name = "afectable_id", nullable = false)
    private Long afectableId;

    @Column(name = "dom_debe_haber", length = 2, nullable = false)
    private String domDebeHaber;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "moneda_id_mo", nullable = false)
    private Long monedaIdMo;

    @Column(name = "monto_mo")
    private BigDecimal montoMo;

    @Column(name = "tipo_cambio")
    private BigDecimal tipoCambio;

    @Column(name = "glosa", length = 100)
    private String glosa;

    public boolean esDebe() {
        return "D".equals(domDebeHaber);
    }

    public Long getId() {
        return id;
    }

    public Long getComprobanteId() {
        return comprobanteId;
    }

    public void setComprobanteId(Long comprobanteId) {
        this.comprobanteId = comprobanteId;
    }

    public Integer getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(Integer secuencia) {
        this.secuencia = secuencia;
    }

    public Long getAfectableId() {
        return afectableId;
    }

    public void setAfectableId(Long afectableId) {
        this.afectableId = afectableId;
    }

    public String getDomDebeHaber() {
        return domDebeHaber;
    }

    public void setDomDebeHaber(String domDebeHaber) {
        this.domDebeHaber = domDebeHaber;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Long getMonedaIdMo() {
        return monedaIdMo;
    }

    public void setMonedaIdMo(Long monedaIdMo) {
        this.monedaIdMo = monedaIdMo;
    }

    public BigDecimal getMontoMo() {
        return montoMo;
    }

    public void setMontoMo(BigDecimal montoMo) {
        this.montoMo = montoMo;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }
}
