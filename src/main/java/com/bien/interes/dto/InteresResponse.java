package com.bien.interes.dto;

import com.bien.interes.entity.InteresCaptacion;
import com.bien.interes.entity.InteresCapitalizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Intereses de una cuenta de captación para la tarjeta del detalle: la tasa que
 * se aplica hoy, lo devengado en el mes en curso, y el historial de devengos y
 * capitalizaciones.
 */
public record InteresResponse(
        BigDecimal devengadoMesActual,
        TasaVigente tasaVigente,
        List<Devengo> devengos,
        List<Capitalizacion> capitalizaciones) {

    public record TasaVigente(BigDecimal tasaAnual, String tipoTasa, Integer baseDias) {
    }

    public record Devengo(
            Long id,
            LocalDate desde,
            LocalDate hasta,
            Integer dias,
            BigDecimal saldoBase,
            BigDecimal tasaAnual,
            BigDecimal interesDevengado,
            String estado) {

        public static Devengo from(InteresCaptacion i) {
            return new Devengo(i.getId(), i.getFechaInicioPeriodo(), i.getFechaFinPeriodo(),
                    i.getDias(), i.getSaldoBase(), i.getTasa(), i.getInteresDevengado(), i.getEstado());
        }
    }

    public record Capitalizacion(
            Long id,
            LocalDateTime fecha,
            BigDecimal montoInteres,
            BigDecimal saldoAnterior,
            BigDecimal saldoPosterior,
            String estado) {

        public static Capitalizacion from(InteresCapitalizacion c) {
            return new Capitalizacion(c.getId(), c.getFechaCapitalizacion(), c.getMontoInteres(),
                    c.getSaldoAnterior(), c.getSaldoPosterior(), c.getEstado());
        }
    }
}
