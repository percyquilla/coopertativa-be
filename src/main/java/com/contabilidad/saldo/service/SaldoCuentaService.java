package com.contabilidad.saldo.service;

import com.bien.common.security.AuditContext;
import com.contabilidad.comprobante.entity.Comprobante;
import com.contabilidad.comprobante.entity.ComprobanteReng;
import com.contabilidad.comprobante.repository.ComprobanteRengRepository;
import com.contabilidad.comprobante.repository.ComprobanteRepository;
import com.contabilidad.cuentaafectable.dto.AfectableEtiqueta;
import com.contabilidad.cuentaafectable.service.CuentaAfectableService;
import com.contabilidad.periodocontable.entity.PeriodoContable;
import com.contabilidad.periodocontable.repository.PeriodoContableRepository;
import com.contabilidad.saldo.dto.BalanceComprobacionResponse;
import com.contabilidad.saldo.dto.MayorResponse;
import com.contabilidad.saldo.dto.SaldoCuentaResponse;
import com.contabilidad.saldo.entity.SaldoCuenta;
import com.contabilidad.saldo.repository.SaldoCuentaRepository;
import com.contabilidad.tipocomprobante.entity.TipoComprobante;
import com.contabilidad.tipocomprobante.repository.TipoComprobanteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mantiene {@code saldo_cuenta}: el acumulado por período de cada cuenta
 * afectable. Se alimenta al autorizar un comprobante ({@link #aplicar}) y al
 * cerrar un período ({@link #arrastrarAlSiguiente}), y se consulta como balance
 * de comprobación o como mayor.
 */
@ApplicationScoped
public class SaldoCuentaService {

    @Inject
    AuditContext audit;

    @Inject
    SaldoCuentaRepository repository;

    @Inject
    PeriodoContableRepository periodoRepository;

    @Inject
    ComprobanteRepository comprobanteRepository;

    @Inject
    ComprobanteRengRepository rengRepository;

    @Inject
    CuentaAfectableService afectableService;

    @Inject
    TipoComprobanteRepository tipoRepository;

    /**
     * Suma los renglones de un comprobante recién autorizado a los saldos de su
     * período. Corre dentro de la misma transacción que la autorización: si algo
     * falla, el comprobante no queda autorizado.
     */
    @Transactional
    public void aplicar(Comprobante comprobante, List<ComprobanteReng> renglones) {
        Long periodoId = comprobante.getPeriodoId();
        PeriodoContable periodo = periodoRepository.findById(periodoId);

        Map<Long, BigDecimal[]> porAfectable = new java.util.HashMap<>();
        for (ComprobanteReng r : renglones) {
            BigDecimal[] dh = porAfectable.computeIfAbsent(r.getAfectableId(),
                    k -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
            if (r.esDebe()) {
                dh[0] = dh[0].add(r.getMonto());
            } else {
                dh[1] = dh[1].add(r.getMonto());
            }
        }

        Map<Long, AfectableEtiqueta> etiquetas = afectableService.etiquetas(porAfectable.keySet());
        String usuario = audit.actor();

        for (Map.Entry<Long, BigDecimal[]> e : porAfectable.entrySet()) {
            Long afectableId = e.getKey();
            BigDecimal debito = e.getValue()[0];
            BigDecimal credito = e.getValue()[1];
            String naturaleza = naturalezaDe(etiquetas.get(afectableId));

            SaldoCuenta saldo = repository.findByPeriodoYAfectable(periodoId, afectableId)
                    .orElseGet(() -> nuevoSaldo(periodo, afectableId));

            saldo.setTotalDebito(saldo.getTotalDebito().add(debito));
            saldo.setTotalCredito(saldo.getTotalCredito().add(credito));
            saldo.setSaldoFinal(calcularSaldoFinal(saldo, naturaleza));
            saldo.setUsuario(usuario);

            if (saldo.getId() == null) {
                repository.persist(saldo);
            }
        }
        repository.getEntityManager().flush();
    }

    /**
     * Al cerrar un período, copia el {@code saldoFinal} de cada cuenta como
     * {@code saldoInicial} del período siguiente. Si el siguiente todavía no
     * existe (última gestión cargada), no hay nada que arrastrar.
     */
    @Transactional
    public void arrastrarAlSiguiente(PeriodoContable cerrado) {
        PeriodoContable siguiente = periodoRepository
                .findSiguiente(cerrado.getGestion(), cerrado.getMes())
                .orElse(null);
        if (siguiente == null) {
            return;
        }

        List<SaldoCuenta> delCerrado = repository.listByPeriodo(cerrado.getId());
        Map<Long, AfectableEtiqueta> etiquetas = afectableService.etiquetas(
                delCerrado.stream().map(SaldoCuenta::getAfectableId).collect(Collectors.toSet()));
        String usuario = audit.actor();

        for (SaldoCuenta origen : delCerrado) {
            String naturaleza = naturalezaDe(etiquetas.get(origen.getAfectableId()));
            SaldoCuenta destino = repository
                    .findByPeriodoYAfectable(siguiente.getId(), origen.getAfectableId())
                    .orElseGet(() -> nuevoSaldo(siguiente, origen.getAfectableId()));

            destino.setSaldoInicial(origen.getSaldoFinal());
            destino.setSaldoFinal(calcularSaldoFinal(destino, naturaleza));
            destino.setUsuario(usuario);

            if (destino.getId() == null) {
                repository.persist(destino);
            }
        }
        repository.getEntityManager().flush();
    }

    public BalanceComprobacionResponse balanceComprobacion(Long periodoId) {
        PeriodoContable periodo = periodoRepository.findByIdOptional(periodoId)
                .orElseThrow(() -> new NotFoundException("Período contable no encontrado: " + periodoId));

        List<SaldoCuenta> saldos = repository.listByPeriodo(periodoId);
        Map<Long, AfectableEtiqueta> etiquetas = afectableService.etiquetas(
                saldos.stream().map(SaldoCuenta::getAfectableId).collect(Collectors.toSet()));

        List<SaldoCuentaResponse> filas = saldos.stream()
                .map(s -> aFila(s, etiquetas.get(s.getAfectableId())))
                .sorted(Comparator.comparing(f -> f.afectableCodigo() == null ? "" : f.afectableCodigo()))
                .toList();

        BigDecimal sumaDebitos = suma(filas, SaldoCuentaResponse::totalDebito);
        BigDecimal sumaCreditos = suma(filas, SaldoCuentaResponse::totalCredito);
        BigDecimal sumaDeudores = suma(filas, SaldoCuentaResponse::saldoDeudor);
        BigDecimal sumaAcreedores = suma(filas, SaldoCuentaResponse::saldoAcreedor);
        boolean cuadra = sumaDebitos.compareTo(sumaCreditos) == 0
                && sumaDeudores.compareTo(sumaAcreedores) == 0;

        return new BalanceComprobacionResponse(periodoId, periodo.getGestion(), periodo.getMes(),
                filas, sumaDebitos, sumaCreditos, sumaDeudores, sumaAcreedores, cuadra);
    }

    public MayorResponse mayor(Long afectableId, Long periodoId) {
        afectableService.findEntityOrThrow(afectableId);
        PeriodoContable periodo = periodoRepository.findByIdOptional(periodoId)
                .orElseThrow(() -> new NotFoundException("Período contable no encontrado: " + periodoId));

        AfectableEtiqueta etiqueta = afectableService.etiquetas(Set.of(afectableId)).get(afectableId);
        String naturaleza = naturalezaDe(etiqueta);

        SaldoCuenta saldo = repository.findByPeriodoYAfectable(periodoId, afectableId).orElse(null);
        BigDecimal saldoInicial = saldo == null ? BigDecimal.ZERO : saldo.getSaldoInicial();

        List<ComprobanteReng> renglones = rengRepository.listAutorizadosDeAfectable(periodoId, afectableId);
        Map<Long, Comprobante> comprobantes = comprobanteRepository
                .listByIds(renglones.stream().map(ComprobanteReng::getComprobanteId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Comprobante::getId, Function.identity()));
        Map<Long, TipoComprobante> tipos = tipoRepository.listAll().stream()
                .collect(Collectors.toMap(TipoComprobante::getId, Function.identity()));

        boolean deudora = "D".equals(naturaleza);
        BigDecimal corrido = saldoInicial;
        List<MayorResponse.Linea> lineas = new ArrayList<>();
        for (ComprobanteReng r : renglones) {
            BigDecimal debe = r.esDebe() ? r.getMonto() : BigDecimal.ZERO;
            BigDecimal haber = r.esDebe() ? BigDecimal.ZERO : r.getMonto();
            corrido = deudora ? corrido.add(debe).subtract(haber) : corrido.subtract(debe).add(haber);

            Comprobante c = comprobantes.get(r.getComprobanteId());
            TipoComprobante t = c == null ? null : tipos.get(c.getTipoComprobanteId());
            String referencia = (c == null || c.getNumero() == null || t == null)
                    ? "#" + r.getComprobanteId()
                    : t.getCodigo() + "-" + c.getNumero();

            lineas.add(new MayorResponse.Linea(
                    r.getComprobanteId(), referencia,
                    c == null ? null : c.getFecha(),
                    c == null ? null : c.getGlosa(),
                    debe, haber, corrido));
        }

        return new MayorResponse(afectableId,
                etiqueta == null ? null : etiqueta.codigo(),
                etiqueta == null ? null : etiqueta.cuentaNombre(),
                naturaleza, periodoId, saldoInicial, corrido, lineas);
    }

    // ---- internos ----

    private SaldoCuenta nuevoSaldo(PeriodoContable periodo, Long afectableId) {
        SaldoCuenta s = new SaldoCuenta();
        s.setPeriodoId(periodo.getId());
        s.setAfectableId(afectableId);
        s.setSaldoInicial(saldoInicialDesdeAnterior(periodo, afectableId));
        s.setTotalDebito(BigDecimal.ZERO);
        s.setTotalCredito(BigDecimal.ZERO);
        s.setSaldoFinal(s.getSaldoInicial());
        s.setEstacion("SISTEMA");
        return s;
    }

    /** El saldo con que abre una cuenta = el saldo final que dejó en el período anterior. */
    private BigDecimal saldoInicialDesdeAnterior(PeriodoContable periodo, Long afectableId) {
        return periodoRepository.findAnterior(periodo.getGestion(), periodo.getMes())
                .flatMap(anterior -> repository.findByPeriodoYAfectable(anterior.getId(), afectableId))
                .map(SaldoCuenta::getSaldoFinal)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calcularSaldoFinal(SaldoCuenta s, String naturaleza) {
        return "D".equals(naturaleza)
                ? s.getSaldoInicial().add(s.getTotalDebito()).subtract(s.getTotalCredito())
                : s.getSaldoInicial().subtract(s.getTotalDebito()).add(s.getTotalCredito());
    }

    private SaldoCuentaResponse aFila(SaldoCuenta s, AfectableEtiqueta etiqueta) {
        String naturaleza = naturalezaDe(etiqueta);
        boolean deudora = "D".equals(naturaleza);
        BigDecimal saldoFinal = s.getSaldoFinal();
        // el saldo se muestra en su columna natural; si quedó del signo contrario,
        // cambia de columna con el valor absoluto
        boolean enColumnaDeudora = deudora == (saldoFinal.signum() >= 0);
        BigDecimal abs = saldoFinal.abs();

        return new SaldoCuentaResponse(
                s.getAfectableId(),
                etiqueta == null ? null : etiqueta.codigo(),
                etiqueta == null ? null : etiqueta.cuentaNombre(),
                naturaleza,
                s.getSaldoInicial(),
                s.getTotalDebito(),
                s.getTotalCredito(),
                saldoFinal,
                enColumnaDeudora ? abs : BigDecimal.ZERO,
                enColumnaDeudora ? BigDecimal.ZERO : abs);
    }

    private static String naturalezaDe(AfectableEtiqueta etiqueta) {
        return etiqueta != null && etiqueta.naturaleza() != null ? etiqueta.naturaleza() : "D";
    }

    private static BigDecimal suma(List<SaldoCuentaResponse> filas, Function<SaldoCuentaResponse, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
