package com.bien.certificadoaportacion.dto;

import java.math.BigDecimal;

/**
 * Valores sugeridos para el formulario de emisión: el próximo número de
 * certificado (referencial — el definitivo se asigna al grabar) y el valor
 * unitario del parámetro {@code VALOR_UNIT_APORTE}.
 */
public record ValoresEmisionResponse(String siguienteNumero, BigDecimal valorUnitario) {
}
