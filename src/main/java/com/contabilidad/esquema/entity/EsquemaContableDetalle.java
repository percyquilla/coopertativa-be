package com.contabilidad.esquema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un renglón de la receta: cuenta afectable, lado (D/H) y la fórmula que dice
 * cuánto va. La fórmula se evalúa contra las variables del contexto de la
 * operación (ver {@link com.contabilidad.motor.EvaluadorFormula}).
 */
@Entity
@Table(name = "esquema_contable_detalle", schema = "sc_contabilidad")
public class EsquemaContableDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "esquema_detalle_id")
    private Long id;

    @Column(name = "esquema_contable_id", nullable = false)
    private Long esquemaContableId;

    @Column(name = "secuencia", nullable = false)
    private Integer secuencia;

    @Column(name = "dom_debe_haber", length = 2)
    private String domDebeHaber;

    @Column(name = "afectable_id", nullable = false)
    private Long afectableId;

    @Column(name = "formula", length = 500)
    private String formula;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    public boolean esDebe() {
        return "D".equals(domDebeHaber);
    }

    public Long getId() {
        return id;
    }

    public Long getEsquemaContableId() {
        return esquemaContableId;
    }

    public void setEsquemaContableId(Long esquemaContableId) {
        this.esquemaContableId = esquemaContableId;
    }

    public Integer getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(Integer secuencia) {
        this.secuencia = secuencia;
    }

    public String getDomDebeHaber() {
        return domDebeHaber;
    }

    public void setDomDebeHaber(String domDebeHaber) {
        this.domDebeHaber = domDebeHaber;
    }

    public Long getAfectableId() {
        return afectableId;
    }

    public void setAfectableId(Long afectableId) {
        this.afectableId = afectableId;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
