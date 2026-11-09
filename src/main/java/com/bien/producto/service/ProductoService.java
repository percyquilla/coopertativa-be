package com.bien.producto.service;

import com.bien.common.security.AuditContext;
import com.bien.cuentacaptacion.repository.CuentaCaptacionRepository;
import com.bien.moneda.entity.Moneda;
import com.bien.moneda.repository.MonedaRepository;
import com.bien.producto.dto.ProductoRequest;
import com.bien.producto.dto.ProductoResponse;
import com.bien.producto.entity.Producto;
import com.bien.producto.repository.ProductoRepository;
import com.bien.productotasa.repository.ProductoTasaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProductoService {

    @Inject
    ProductoRepository productoRepository;

    @Inject
    CuentaCaptacionRepository cuentaCaptacionRepository;

    @Inject
    MonedaRepository monedaRepository;

    @Inject
    ProductoTasaRepository tasaRepository;

    @Inject
    AuditContext audit;

    public List<ProductoResponse> listAll() {
        Map<Long, Long> cuentasPorProducto = cuentaCaptacionRepository.countAgrupadoPorProducto();
        Map<Long, Long> tasasVigentes = tasaRepository.countVigentesPorProducto(LocalDate.now());
        return productoRepository.list("order by codigo").stream()
                .map(p -> ProductoResponse.from(p,
                        cuentasPorProducto.getOrDefault(p.getId(), 0L),
                        tasasVigentes.containsKey(p.getId())))
                .toList();
    }

    public ProductoResponse findById(Long id) {
        Producto p = findEntityOrThrow(id);
        return ProductoResponse.from(p,
                cuentaCaptacionRepository.countByProducto(id),
                tasaRepository.findVigente(id, LocalDate.now()).isPresent());
    }

    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        if (productoRepository.existsByCodigo(request.codigo())) {
            throw conflicto("Ya existe un producto con el código " + request.codigo() + ".");
        }
        String estado = request.estado() != null ? request.estado() : "V";
        validarComun(request, estado, null);

        Producto p = new Producto();
        p.setCodigo(request.codigo());
        p.setTipoProducto(request.tipoProducto());
        p.setMoneda(request.moneda());
        aplicarEditables(p, request, estado);
        p.setUsuario(audit.actor());
        p.setEstacion(audit.estacion());

        productoRepository.persist(p);
        productoRepository.getEntityManager().flush();
        productoRepository.getEntityManager().refresh(p);
        return ProductoResponse.from(p, 0L);
    }

    @Transactional
    public ProductoResponse update(Long id, ProductoRequest request) {
        Producto p = findEntityOrThrow(id);
        long cuentas = cuentaCaptacionRepository.countByProducto(id);

        if (productoRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw conflicto("Ya existe un producto con el código " + request.codigo() + ".");
        }
        // La cuenta copia el tipo y la moneda al abrirse: cambiarlos después
        // dejaría las cuentas existentes describiendo un producto que ya no es.
        if (cuentas > 0 && !p.getTipoProducto().equals(request.tipoProducto())) {
            throw conflicto("No se puede cambiar el tipo de un producto que ya tiene " + cuentasTexto(cuentas));
        }
        if (cuentas > 0 && !p.getMoneda().equals(request.moneda())) {
            throw conflicto("No se puede cambiar la moneda de un producto que ya tiene " + cuentasTexto(cuentas));
        }

        String estado = request.estado() != null ? request.estado() : p.getEstado();
        validarComun(request, estado, id);

        p.setCodigo(request.codigo());
        p.setTipoProducto(request.tipoProducto());
        p.setMoneda(request.moneda());
        aplicarEditables(p, request, estado);
        p.setUsuario(audit.actor());
        p.setEstacion(audit.estacion());

        return ProductoResponse.from(p, cuentas);
    }

    public Producto findEntityOrThrow(Long id) {
        return productoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + id));
    }

    /**
     * Producto vigente del tipo pedido. Lo usan los módulos que operan sobre un
     * tipo de producto fijo (aportaciones → "CA") en vez de un id concreto.
     */
    public Producto findVigenteByTipoOrThrow(String tipoProducto) {
        return productoRepository.findVigenteByTipo(tipoProducto)
                .orElseThrow(() -> new NotFoundException(
                        "No hay un producto vigente de tipo " + tipoProducto));
    }

    // ------------------------------------------------------------------

    private void validarComun(ProductoRequest request, String estado, Long idActual) {
        if (request.fechaFin() != null && request.fechaFin().isBefore(request.fechaInicio())) {
            throw new WebApplicationException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.",
                    Response.Status.BAD_REQUEST);
        }

        Moneda moneda = monedaRepository.findByCodigo(request.moneda())
                .orElseThrow(() -> new NotFoundException(
                        "No existe la moneda " + request.moneda() + "."));
        if (!"V".equals(moneda.getEstado())) {
            throw conflicto("La moneda " + request.moneda()
                    + " está suspendida; no se puede usar en un producto.");
        }

        if ("CA".equals(request.tipoProducto())) {
            if (request.permiteRetiro()) {
                throw conflicto("Los certificados de aportación no admiten retiro directo: "
                        + "el capital solo se devuelve con una solicitud de retiro.");
            }
            // Aportaciones resuelve el producto por tipo, no por id: dos vigentes
            // harían que la elección dependiera del orden de código.
            if ("V".equals(estado)
                    && productoRepository.existsVigenteByTipoAndIdNot("CA", idActual)) {
                throw conflicto("Ya hay un producto vigente de certificado de aportación. "
                        + "Suspendé el actual antes de poner otro en vigencia.");
            }
        }
    }

    private void aplicarEditables(Producto p, ProductoRequest request, String estado) {
        p.setNombre(request.nombre());
        p.setDescripcion(request.descripcion());
        p.setSaldoMinimo(request.saldoMinimo());
        p.setPermiteDeposito(request.permiteDeposito());
        p.setPermiteRetiro(request.permiteRetiro());
        p.setPermiteInteres(request.permiteInteres());
        p.setFechaInicio(request.fechaInicio());
        p.setEstado(estado);

        if ("S".equals(estado)) {
            // Suspender cierra la vigencia; las cuentas ya abiertas siguen operando.
            p.setFechaFin(request.fechaFin() != null ? request.fechaFin() : LocalDate.now());
        } else {
            p.setFechaFin(request.fechaFin());
        }
    }

    /** El conteo incluye cuentas cerradas: también copiaron tipo y moneda al abrirse. */
    private String cuentasTexto(long cuentas) {
        return cuentas == 1 ? "1 cuenta registrada (incluidas las cerradas)."
                : cuentas + " cuentas registradas (incluidas las cerradas).";
    }

    private WebApplicationException conflicto(String mensaje) {
        return new WebApplicationException(mensaje, Response.Status.CONFLICT);
    }
}
