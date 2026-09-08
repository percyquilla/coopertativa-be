package com.contabilidad.motor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Todo lo que el motor necesita para contabilizar un hecho de negocio: qué tipo
 * de operación es, cuándo pasó, de dónde vino, y las variables que sus fórmulas
 * pueden usar.
 *
 * @param codigoOperacion código del {@code tipo_operacion} ("DEP_AHO", "APO_DEV"…)
 * @param fecha           fecha del hecho — define el período del asiento
 * @param glosa           texto del comprobante
 * @param origenModulo    módulo que originó el hecho ("captaciones", "aportaciones")
 * @param origenTabla     tabla de la fila de origen ("movimiento", "certificado_aportacion")
 * @param origenId        id de esa fila, para poder reprocesar y para trazar
 * @param variables       nombres en MAYÚSCULAS → valor, para las fórmulas ({@code MONTO}, …)
 */
public record ContextoContable(
        String codigoOperacion,
        LocalDate fecha,
        String glosa,
        String origenModulo,
        String origenTabla,
        Long origenId,
        Map<String, BigDecimal> variables) {

    public static ContextoContable de(String codigoOperacion, LocalDate fecha, String glosa,
                                      String origenModulo, String origenTabla, Long origenId,
                                      BigDecimal monto) {
        return new ContextoContable(codigoOperacion, fecha, glosa, origenModulo, origenTabla, origenId,
                Map.of("MONTO", monto));
    }
}
