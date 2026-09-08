package com.bien.cuentacaptacion.service;

import com.bien.cuentacaptacion.dto.CuentaCaptacionRequest;
import com.bien.cuentacaptacion.dto.CuentaCaptacionResponse;
import com.bien.cuentacaptacion.entity.CuentaCaptacion;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.producto.entity.Producto;
import com.bien.producto.service.ProductoService;
import jakarta.enterprise.context.ApplicationScoped;
import com.bien.common.security.AuditContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CuentaCaptacionService {

    @Inject
    AuditContext audit;

    @Inject
    CuentaCaptacionRepository repository;

    @Inject
    ProductoService productoService;

    public List<CuentaCaptacionResponse> listAll() {
        return repository.list("order by numeroCuenta").stream()
                .map(CuentaCaptacionResponse::from)
                .toList();
    }

    public CuentaCaptacionResponse findById(Long id) {
        return CuentaCaptacionResponse.from(findEntityOrThrow(id));
    }

    public CuentaCaptacion findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Cuenta de captación no encontrada: " + id));
    }

    @Transactional
    public CuentaCaptacionResponse create(CuentaCaptacionRequest request) {
        Producto producto = productoService.findEntityOrThrow(request.productoId());
        if (!producto.estaVigente(LocalDate.now())) {
            throw new WebApplicationException(
                    "El producto " + producto.getCodigo() + " no está vigente; no se pueden abrir cuentas con él.",
                    Response.Status.CONFLICT);
        }
        if (repository.existsByNumeroCuenta(request.numeroCuenta())) {
            throw new WebApplicationException("El número de cuenta ya existe: " + request.numeroCuenta(),
                    Response.Status.CONFLICT);
        }

        return CuentaCaptacionResponse.from(
                abrir(producto, request.numeroCuenta(), request.tipoCuenta(), audit.actor()));
    }

    /**
     * Apertura a nivel de entidad, para que otros módulos abran cuentas sin pasar
     * por el DTO de request (p. ej. aportaciones abre la cuenta CA del socio).
     * Asume que el producto ya fue validado y que el número no está repetido.
     */
    public CuentaCaptacion abrir(Producto producto, String numeroCuenta, String tipoCuenta, String usuario) {
        CuentaCaptacion c = new CuentaCaptacion();
        c.setNumeroCuenta(numeroCuenta);
        c.setProductoId(producto.getId());
        c.setTipoCuenta(tipoCuenta);
        c.setMoneda(producto.getMoneda());
        c.setFechaApertura(LocalDate.now());
        c.setSaldoActual(BigDecimal.ZERO);
        c.setSaldoBloqueado(BigDecimal.ZERO);
        c.setEstadoCuenta("A");
        c.setUsuario(usuario);
        c.setEstacion("SISTEMA");

        repository.persist(c);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(c);
        return c;
    }

    public boolean existsByNumeroCuenta(String numeroCuenta) {
        return repository.existsByNumeroCuenta(numeroCuenta);
    }

    @Transactional
    public CuentaCaptacionResponse cerrar(Long id) {
        CuentaCaptacion c = findEntityOrThrow(id);
        if ("C".equals(c.getEstadoCuenta())) {
            throw new WebApplicationException("La cuenta ya está cerrada", Response.Status.CONFLICT);
        }
        if (c.getSaldoActual().compareTo(BigDecimal.ZERO) != 0) {
            throw new WebApplicationException("No se puede cerrar una cuenta con saldo distinto de cero",
                    Response.Status.CONFLICT);
        }
        c.setEstadoCuenta("C");
        c.setFechaCierre(LocalDate.now());
        return CuentaCaptacionResponse.from(c);
    }
}
