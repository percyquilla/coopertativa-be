package com.bien.movimiento.service;

import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import com.bien.cuentacaptacion.service.CuentaCaptacionService;
import com.bien.movimiento.dto.MovimientoRequest;
import com.bien.movimiento.dto.MovimientoResponse;
import com.bien.movimiento.entity.Movimiento;
import com.bien.movimiento.repository.MovimientoRepository;
import com.bien.producto.entity.Producto;
import com.bien.producto.service.ProductoService;
import com.contabilidad.motor.ContabilizadorService;
import com.contabilidad.motor.ContextoContable;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class MovimientoService {

    @Inject
    AuditContext audit;

    @Inject
    MovimientoRepository repository;

    @Inject
    CuentaCaptacionService cuentaService;

    @Inject
    ProductoService productoService;

    @Inject
    ContabilizadorService contabilizador;

    public List<MovimientoResponse> listByCuenta(Long cuentaCaptacionId) {
        cuentaService.findEntityOrThrow(cuentaCaptacionId);
        return repository.listByCuenta(cuentaCaptacionId).stream()
                .map(MovimientoResponse::from)
                .toList();
    }

    @Transactional
    public MovimientoResponse registrar(Long cuentaCaptacionId, MovimientoRequest request) {
        CuentaCaptacion cuenta = cuentaService.findEntityOrThrow(cuentaCaptacionId);
        requireActiva(cuenta);

        Producto producto = productoService.findEntityOrThrow(cuenta.getProductoId());
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal monto = request.monto();
        BigDecimal saldoPosterior;

        if ("DEP".equals(request.tipoMovimiento())) {
            if (!producto.isPermiteDeposito()) {
                throw new BadRequestException("El producto no permite depósitos");
            }
            saldoPosterior = saldoAnterior.add(monto);
        } else { // RET
            if (!producto.isPermiteRetiro()) {
                throw new BadRequestException("El producto no permite retiros");
            }
            saldoPosterior = calcularRetiro(cuenta, monto, producto.getSaldoMinimo());
        }

        MovimientoResponse respuesta = grabar(cuenta, request.tipoMovimiento(), monto, saldoAnterior,
                saldoPosterior, request.referencia(), request.descripcion(), audit.actor());

        // asiento automático: el código de operación depende del tipo de producto
        // (un depósito en una cuenta de aportaciones mueve capital, no ahorro)
        boolean esCapital = "CA".equals(producto.getTipoProducto());
        String codigoOperacion = "DEP".equals(request.tipoMovimiento())
                ? (esCapital ? "APO_EMI" : "DEP_AHO")
                : "RET_AHO";
        String glosa = ("DEP".equals(request.tipoMovimiento()) ? "Depósito" : "Retiro")
                + " en la cuenta " + cuenta.getNumeroCuenta();
        contabilizador.contabilizar(ContextoContable.de(codigoOperacion, LocalDate.now(), glosa,
                "captaciones", "movimiento", respuesta.id(), monto));

        return respuesta;
    }

    /**
     * Retiro institucional: la única vía por la que sale plata de un producto con
     * {@code permite_retiro = false} — hoy, las aportaciones (tipo CA), que solo se
     * devuelven contra una solicitud de retiro aprobada.
     * <p>
     * Salta el permiso del producto y el saldo mínimo, pero NO el chequeo de cuenta
     * activa ni el de saldo disponible. No se expone por REST: solo lo invocan los
     * servicios que implementan un flujo con su propia autorización.
     */
    @Transactional
    public MovimientoResponse registrarRetiroInstitucional(Long cuentaCaptacionId, BigDecimal monto,
                                                           String referencia, String descripcion, String usuario) {
        CuentaCaptacion cuenta = cuentaService.findEntityOrThrow(cuentaCaptacionId);
        requireActiva(cuenta);

        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoPosterior = calcularRetiro(cuenta, monto, null);

        return grabar(cuenta, "RET", monto, saldoAnterior, saldoPosterior, referencia, descripcion, usuario);
    }

    /**
     * Capitalización de intereses (Fase 6): suma {@code monto} al saldo de la
     * cuenta con un movimiento tipo {@code INT}. No pasa por {@code permite_deposito}
     * (el interés lo genera el producto, no un depósito del socio) pero sí exige
     * la cuenta activa. No se expone por REST: solo la llama {@code InteresService}.
     */
    @Transactional
    public MovimientoResponse registrarCapitalizacionInteres(Long cuentaCaptacionId, BigDecimal monto,
                                                             String referencia, String descripcion,
                                                             String usuario) {
        CuentaCaptacion cuenta = cuentaService.findEntityOrThrow(cuentaCaptacionId);
        requireActiva(cuenta);

        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoPosterior = saldoAnterior.add(monto);

        return grabar(cuenta, "INT", monto, saldoAnterior, saldoPosterior, referencia, descripcion, usuario);
    }

    // ---- internos ----

    private void requireActiva(CuentaCaptacion cuenta) {
        if (!"A".equals(cuenta.getEstadoCuenta())) {
            String detalle = "C".equals(cuenta.getEstadoCuenta()) ? "La cuenta está cerrada."
                    : "I".equals(cuenta.getEstadoCuenta()) ? "La cuenta está inactiva."
                    : "La cuenta no está activa.";
            throw new WebApplicationException(detalle + " No se pueden registrar movimientos.",
                    Response.Status.CONFLICT);
        }
    }

    /**
     * Saldo resultante de un retiro. {@code saldoMinimo} null = sin mínimo a respetar.
     * Un retiro total (deja la cuenta en cero, p. ej. para cerrarla) también puede
     * bajar del mínimo; cualquier saldo remanente sí debe respetarlo.
     */
    private BigDecimal calcularRetiro(CuentaCaptacion cuenta, BigDecimal monto, BigDecimal saldoMinimo) {
        BigDecimal disponible = cuenta.getSaldoActual().subtract(cuenta.getSaldoBloqueado());
        if (monto.compareTo(disponible) > 0) {
            throw new BadRequestException("Monto mayor al saldo disponible (" + disponible + ")");
        }
        BigDecimal saldoPosterior = cuenta.getSaldoActual().subtract(monto);
        boolean vaciaLaCuenta = saldoPosterior.compareTo(BigDecimal.ZERO) == 0;
        if (saldoMinimo != null && !vaciaLaCuenta && saldoPosterior.compareTo(saldoMinimo) < 0) {
            throw new BadRequestException(
                    "El retiro dejaría el saldo por debajo del mínimo del producto (" + saldoMinimo + ")");
        }
        return saldoPosterior;
    }

    private MovimientoResponse grabar(CuentaCaptacion cuenta, String tipoMovimiento, BigDecimal monto,
                                      BigDecimal saldoAnterior, BigDecimal saldoPosterior,
                                      String referencia, String descripcion, String usuario) {
        Movimiento m = new Movimiento();
        m.setCuentaCaptacionId(cuenta.getId());
        m.setTipoMovimiento(tipoMovimiento);
        m.setMonto(monto);
        m.setSaldoAnterior(saldoAnterior);
        m.setSaldoPosterior(saldoPosterior);
        m.setReferencia(referencia);
        m.setDescripcion(descripcion);
        m.setEstadoMov("REG");
        m.setUsuario(usuario);
        repository.persist(m);

        cuenta.setSaldoActual(saldoPosterior);

        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(m);
        return MovimientoResponse.from(m);
    }
}
