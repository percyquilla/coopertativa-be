package com.bien.productotasa.service;

import com.bien.common.security.AuditContext;
import com.bien.producto.entity.Producto;
import com.bien.producto.service.ProductoService;
import com.bien.productotasa.dto.ProductoTasaRequest;
import com.bien.productotasa.dto.ProductoTasaResponse;
import com.bien.productotasa.entity.ProductoTasa;
import com.bien.productotasa.repository.ProductoTasaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class ProductoTasaService {

    @Inject
    ProductoTasaRepository repository;

    @Inject
    ProductoService productoService;

    @Inject
    AuditContext audit;

    public List<ProductoTasaResponse> listByProducto(Long productoId) {
        productoService.findEntityOrThrow(productoId);
        return repository.listByProducto(productoId).stream()
                .map(ProductoTasaResponse::from)
                .toList();
    }

    @Transactional
    public ProductoTasaResponse create(Long productoId, ProductoTasaRequest request) {
        Producto producto = productoService.findEntityOrThrow(productoId);
        validar(producto, productoId, request, null);

        ProductoTasa t = new ProductoTasa();
        t.setProductoId(productoId);
        aplicar(t, request);
        t.setUsuario(audit.actor());
        t.setEstacion(audit.estacion());

        repository.persist(t);
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(t);
        return ProductoTasaResponse.from(t);
    }

    @Transactional
    public ProductoTasaResponse update(Long productoId, Long tasaId, ProductoTasaRequest request) {
        Producto producto = productoService.findEntityOrThrow(productoId);
        ProductoTasa t = findEntityOrThrow(tasaId);
        if (!t.getProductoId().equals(productoId)) {
            throw new NotFoundException("La tasa " + tasaId + " no pertenece a ese producto.");
        }
        validar(producto, productoId, request, tasaId);

        aplicar(t, request);
        t.setUsuario(audit.actor());
        t.setEstacion(audit.estacion());
        return ProductoTasaResponse.from(t);
    }

    // ------------------------------------------------------------------

    private void validar(Producto producto, Long productoId, ProductoTasaRequest request, Long excluirId) {
        if (!producto.isPermiteInteres()) {
            throw conflicto("El producto " + producto.getCodigo() + " no genera intereses. "
                    + "Activá «Genera intereses» en el producto antes de cargarle una tasa.");
        }
        if (request.fechaFin() != null && request.fechaFin().isBefore(request.fechaInicio())) {
            throw new WebApplicationException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.",
                    Response.Status.BAD_REQUEST);
        }
        boolean vigente = request.estado() == null || "V".equals(request.estado());
        if (vigente && repository.existsSolapada(productoId, request.fechaInicio(), request.fechaFin(), excluirId)) {
            throw conflicto("Ya hay otra tasa vigente para este producto en esas fechas. "
                    + "Cerrale la vigencia a la anterior o suspendela antes de cargar esta.");
        }
    }

    private void aplicar(ProductoTasa t, ProductoTasaRequest request) {
        t.setTasa(request.tasa());
        t.setTipoTasa(request.tipoTasa());
        t.setBaseDias(Integer.valueOf(request.baseDias()));
        t.setFechaInicio(request.fechaInicio());
        t.setFechaFin(request.fechaFin());
        t.setEstado(request.estado() != null ? request.estado() : "V");
    }

    private ProductoTasa findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Tasa no encontrada: " + id));
    }

    private WebApplicationException conflicto(String mensaje) {
        return new WebApplicationException(mensaje, Response.Status.CONFLICT);
    }
}
