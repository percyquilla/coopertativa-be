package com.contabilidad.reporte.service;

import com.contabilidad.cuentaafectable.entity.CuentaAfectable;
import com.contabilidad.cuentaafectable.repository.CuentaAfectableRepository;
import com.contabilidad.cuentacontable.entity.CuentaContable;
import com.contabilidad.cuentacontable.repository.CuentaContableRepository;
import com.contabilidad.motor.EvaluadorFormula;
import com.contabilidad.periodocontable.entity.PeriodoContable;
import com.contabilidad.periodocontable.repository.PeriodoContableRepository;
import com.contabilidad.reporte.dto.ReporteResponse;
import com.contabilidad.reporte.entity.EstructuraReporte;
import com.contabilidad.reporte.entity.EstructuraReporteCuenta;
import com.contabilidad.reporte.entity.EstructuraReporteLinea;
import com.contabilidad.reporte.repository.EstructuraReporteRepository;
import com.contabilidad.saldo.entity.SaldoCuenta;
import com.contabilidad.saldo.repository.SaldoCuentaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Arma el Estado de Situación Financiera y el Estado de Resultados de un período
 * a partir de {@code estructura_reporte_*} y los saldos de {@code saldo_cuenta}.
 */
@ApplicationScoped
public class ReporteService {

    @Inject
    EstructuraReporteRepository estructuraRepository;

    @Inject
    PeriodoContableRepository periodoRepository;

    @Inject
    CuentaContableRepository cuentaRepository;

    @Inject
    CuentaAfectableRepository afectableRepository;

    @Inject
    SaldoCuentaRepository saldoRepository;

    public ReporteResponse esf(Long periodoId) {
        return armar("ESF", periodoId);
    }

    public ReporteResponse er(Long periodoId) {
        return armar("ER", periodoId);
    }

    // ---- internos ----

    private ReporteResponse armar(String codigoReporte, Long periodoId) {
        EstructuraReporte estructura = estructuraRepository.findByCodigo(codigoReporte)
                .orElseThrow(() -> new NotFoundException("Estructura de reporte no encontrada: " + codigoReporte));
        PeriodoContable periodo = periodoRepository.findByIdOptional(periodoId)
                .orElseThrow(() -> new NotFoundException("Período contable no encontrado: " + periodoId));

        List<EstructuraReporteLinea> lineas = estructuraRepository.lineasDe(estructura.getId());
        Map<Long, List<EstructuraReporteCuenta>> cuentasPorLinea = estructuraRepository
                .cuentasDe(lineas.stream().map(EstructuraReporteLinea::getId).toList())
                .stream().collect(Collectors.groupingBy(EstructuraReporteCuenta::getEstructuraReporteLineaId));

        Map<Long, BigDecimal> saldoPorCuenta = saldoNaturalPorCuenta(periodoId);

        List<ReporteResponse.Linea> filas = new ArrayList<>();
        Map<String, BigDecimal> montoPorCodigo = new HashMap<>();

        for (EstructuraReporteLinea linea : lineas) {
            BigDecimal monto;
            switch (linea.getTipoLinea()) {
                case "TITULO" -> monto = null;
                case "DETALLE" -> {
                    BigDecimal suma = BigDecimal.ZERO;
                    for (EstructuraReporteCuenta m : cuentasPorLinea.getOrDefault(linea.getId(), List.of())) {
                        BigDecimal valor = saldoPorCuenta.getOrDefault(m.getCuentaId(), BigDecimal.ZERO);
                        suma = suma.add(valor.multiply(BigDecimal.valueOf(m.getSigno())));
                    }
                    monto = suma.multiply(BigDecimal.valueOf(linea.getSigno()));
                }
                default -> { // SUBTOTAL / TOTAL
                    BigDecimal valor = EvaluadorFormula.evaluar(linea.getFormula(), montoPorCodigo,
                            "reporte " + codigoReporte + ", línea " + linea.getCodigo());
                    monto = valor.multiply(BigDecimal.valueOf(linea.getSigno()));
                }
            }
            BigDecimal redondeado = monto == null ? null : monto.setScale(2, RoundingMode.HALF_UP);
            montoPorCodigo.put(linea.getCodigo(), redondeado == null ? BigDecimal.ZERO : redondeado);
            filas.add(new ReporteResponse.Linea(linea.getCodigo(), linea.getTitulo(),
                    linea.getTipoLinea(), "DETALLE".equals(linea.getTipoLinea()) ? 1 : 0, redondeado));
        }

        BigDecimal descuadre = "ESF".equals(estructura.getTipo())
                ? montoPorCodigo.getOrDefault("ESF_TOT_ACT", BigDecimal.ZERO)
                        .subtract(montoPorCodigo.getOrDefault("ESF_TOT_PP", BigDecimal.ZERO))
                : null;

        return new ReporteResponse(estructura.getCodigo(), estructura.getNombre(), estructura.getTipo(),
                periodoId, periodo.getGestion(), periodo.getMes(), filas, descuadre);
    }

    /**
     * Saldo (ya normalizado por naturaleza) de cada cuenta contable en el
     * período: los de las cuentas de mayor salen de sus afectables, y las
     * agrupadoras acumulan el de sus descendientes.
     */
    private Map<Long, BigDecimal> saldoNaturalPorCuenta(Long periodoId) {
        Map<Long, Long> cuentaDeAfectable = afectableRepository.listOrdenadas().stream()
                .collect(Collectors.toMap(CuentaAfectable::getId, CuentaAfectable::getCuentaId));

        Map<Long, BigDecimal> acumulado = new HashMap<>();
        for (SaldoCuenta s : saldoRepository.listByPeriodo(periodoId)) {
            Long cuentaId = cuentaDeAfectable.get(s.getAfectableId());
            if (cuentaId != null) {
                acumulado.merge(cuentaId, s.getSaldoFinal(), BigDecimal::add);
            }
        }

        // de las hojas a la raíz: cada cuenta suma su acumulado al de su padre
        List<CuentaContable> cuentas = new ArrayList<>(cuentaRepository.listOrdenadas());
        cuentas.sort(Comparator.comparing(CuentaContable::getNivel).reversed());
        for (CuentaContable c : cuentas) {
            if (c.getCuentaPadreId() != null) {
                BigDecimal propio = acumulado.getOrDefault(c.getId(), BigDecimal.ZERO);
                if (propio.signum() != 0) {
                    acumulado.merge(c.getCuentaPadreId(), propio, BigDecimal::add);
                }
            }
        }
        return acumulado;
    }
}
