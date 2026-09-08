package com.bien.interes.service;

import com.bien.common.security.AuditContext;
import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.interes.entity.InteresCaptacion;
import com.bien.interes.entity.InteresCapitalizacion;
import com.bien.interes.dto.InteresResponse;
import com.bien.interes.repository.InteresCaptacionRepository;
import com.bien.interes.repository.InteresCapitalizacionRepository;
import com.bien.movimiento.dto.MovimientoResponse;
import com.bien.movimiento.service.MovimientoService;
import com.bien.productotasa.entity.ProductoTasa;
import com.bien.productotasa.repository.ProductoTasaRepository;
import com.contabilidad.motor.ContabilizadorService;
import com.contabilidad.motor.ContextoContable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fase 6 — intereses de caja de ahorro. Lo llama {@code DiaContableService.cerrar}:
 * cada cierre de día devenga el interés del día (asiento DEV_INT), y el cierre del
 * último día del mes capitaliza lo devengado (movimiento INT + asiento CAP_INT).
 * Ambos pasos son idempotentes y no bloqueantes.
 */
@ApplicationScoped
public class InteresService {

    private static final Logger LOG = Logger.getLogger(InteresService.class);
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");
    private static final BigDecimal CIEN = new BigDecimal("100");

    @Inject
    AuditContext audit;

    @Inject
    CuentaCaptacionRepository cuentaRepository;

    @Inject
    ProductoTasaRepository tasaRepository;

    @Inject
    InteresCaptacionRepository captacionRepository;

    @Inject
    InteresCapitalizacionRepository capitalizacionRepository;

    @Inject
    MovimientoService movimientoService;

    @Inject
    ContabilizadorService contabilizador;

    /**
     * Devenga el interés de {@code fecha} para cada cuenta con producto que genera
     * intereses, tasa vigente y saldo positivo. Acumula en la fila del mes y emite
     * un asiento DEV_INT por el importe del día. Salta las cuentas ya devengadas
     * ese día.
     *
     * @return cuántas cuentas devengaron ese día
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int devengarDia(LocalDate fecha) {
        String usuario = audit.actor();
        Map<Long, Optional<ProductoTasa>> tasaPorProducto = new HashMap<>();
        int devengaron = 0;

        for (CuentaCaptacion cuenta : cuentaRepository.listParaDevengo()) {
            Optional<ProductoTasa> tasaOpt = tasaPorProducto.computeIfAbsent(
                    cuenta.getProductoId(), pid -> tasaRepository.findVigente(pid, fecha));
            if (tasaOpt.isEmpty()) {
                continue;
            }
            ProductoTasa tasa = tasaOpt.get();

            InteresCaptacion fila = captacionRepository.findDelMes(cuenta.getId(), fecha).orElse(null);
            if (fila != null && (!"REG".equals(fila.getEstado())
                    || !fila.getFechaFinPeriodo().isBefore(fecha))) {
                continue; // mes ya capitalizado, o el día ya se devengó
            }

            BigDecimal interesDia = cuenta.getSaldoActual()
                    .multiply(tasaDiaria(tasa))
                    .setScale(2, RoundingMode.HALF_UP);
            if (interesDia.signum() <= 0) {
                continue;
            }

            if (fila == null) {
                fila = new InteresCaptacion();
                fila.setCuentaCaptacionId(cuenta.getId());
                fila.setFechaInicioPeriodo(fecha.withDayOfMonth(1));
                fila.setTasa(tasa.getTasa());
                fila.setDias(1);
                fila.setInteresDevengado(interesDia);
                fila.setEstado("REG");
            } else {
                fila.setDias(fila.getDias() + 1);
                fila.setInteresDevengado(fila.getInteresDevengado().add(interesDia));
            }
            fila.setFechaFinPeriodo(fecha);
            fila.setSaldoBase(cuenta.getSaldoActual());
            fila.setInteresCalculado(fila.getInteresDevengado());
            fila.setUsuario(usuario);
            captacionRepository.persist(fila);
            captacionRepository.getEntityManager().flush();

            String glosa = "Devengo de intereses " + fecha + " — cuenta " + cuenta.getNumeroCuenta();
            contabilizador.contabilizar(ContextoContable.de("DEV_INT", fecha, glosa,
                    "captaciones", "interes_captacion", fila.getId(), interesDia));
            devengaron++;
        }

        if (devengaron > 0) {
            LOG.infof("Devengo de intereses %s: %d cuenta(s)", fecha, devengaron);
        }
        return devengaron;
    }

    /**
     * Capitaliza el interés devengado del mes que contiene {@code fecha}: por cada
     * devengo 'REG' con importe, suma el interés al saldo (movimiento INT), registra
     * la capitalización y emite el asiento CAP_INT. Idempotente: los devengos ya
     * capitalizados quedan en 'CAP' y no se vuelven a tocar.
     *
     * @return cuántas cuentas capitalizaron
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int capitalizarMes(LocalDate fecha) {
        String usuario = audit.actor();
        String periodo = fecha.format(YM);
        int capitalizaron = 0;

        for (InteresCaptacion fila : captacionRepository.listRegDelMes(fecha)) {
            BigDecimal monto = fila.getInteresDevengado();
            CuentaCaptacion cuenta = cuentaRepository.findById(fila.getCuentaCaptacionId());
            if (cuenta == null) {
                continue;
            }

            String glosa = "Capitalización de intereses " + periodo + " — cuenta "
                    + cuenta.getNumeroCuenta();
            MovimientoResponse mov = movimientoService.registrarCapitalizacionInteres(
                    cuenta.getId(), monto, "CAP-INT-" + periodo, glosa, usuario);

            InteresCapitalizacion cap = new InteresCapitalizacion();
            cap.setInteresCaptacionId(fila.getId());
            cap.setCuentaCaptacionId(cuenta.getId());
            cap.setMontoInteres(monto);
            cap.setSaldoAnterior(mov.saldoAnterior());
            cap.setSaldoPosterior(mov.saldoPosterior());
            cap.setEstado("REG");
            cap.setUsuario(usuario);
            capitalizacionRepository.persist(cap);

            fila.setEstado("CAP");
            captacionRepository.getEntityManager().flush();

            contabilizador.contabilizar(ContextoContable.de("CAP_INT", fecha, glosa,
                    "captaciones", "interes_capitalizacion", cap.getId(), monto));
            capitalizaron++;
        }

        if (capitalizaron > 0) {
            LOG.infof("Capitalización de intereses %s: %d cuenta(s)", periodo, capitalizaron);
        }
        return capitalizaron;
    }

    // ---- lectura para la pantalla ----

    public InteresResponse resumenDe(Long cuentaCaptacionId) {
        CuentaCaptacion cuenta = cuentaRepository.findById(cuentaCaptacionId);
        if (cuenta == null) {
            throw new NotFoundException("Cuenta de captación no encontrada: " + cuentaCaptacionId);
        }
        LocalDate hoy = LocalDate.now();

        BigDecimal devengadoMes = captacionRepository.findDelMes(cuentaCaptacionId, hoy)
                .map(InteresCaptacion::getInteresDevengado)
                .orElse(BigDecimal.ZERO);

        InteresResponse.TasaVigente tasaVigente = tasaRepository
                .findVigente(cuenta.getProductoId(), hoy)
                .map(t -> new InteresResponse.TasaVigente(t.getTasa(), t.getTipoTasa(), t.getBaseDias()))
                .orElse(null);

        List<InteresResponse.Devengo> devengos = captacionRepository.listByCuenta(cuentaCaptacionId)
                .stream().map(InteresResponse.Devengo::from).toList();
        List<InteresResponse.Capitalizacion> capitalizaciones = capitalizacionRepository
                .listByCuenta(cuentaCaptacionId).stream()
                .map(InteresResponse.Capitalizacion::from).toList();

        return new InteresResponse(devengadoMes, tasaVigente, devengos, capitalizaciones);
    }

    /** Cuántas cuentas devengaron intereses en {@code fecha}; para el cierre diario. */
    public long devengadosEn(LocalDate fecha) {
        return captacionRepository.countDevengadosEn(fecha);
    }

    // ---- cálculo ----

    /** Tasa diaria a partir de la tasa anual (%) y la base de días; N lineal, E compuesta. */
    private BigDecimal tasaDiaria(ProductoTasa tasa) {
        BigDecimal anualFrac = tasa.getTasa().divide(CIEN, 12, RoundingMode.HALF_UP);
        int base = tasa.getBaseDias();
        if ("E".equals(tasa.getTipoTasa())) {
            double diaria = Math.pow(1 + anualFrac.doubleValue(), 1.0 / base) - 1;
            return BigDecimal.valueOf(diaria).setScale(12, RoundingMode.HALF_UP);
        }
        return anualFrac.divide(BigDecimal.valueOf(base), 12, RoundingMode.HALF_UP);
    }
}
