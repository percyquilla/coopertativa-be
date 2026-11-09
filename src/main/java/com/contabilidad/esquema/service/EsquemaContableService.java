package com.contabilidad.esquema.service;

import com.bien.common.security.AuditContext;
import com.contabilidad.cuentaafectable.dto.AfectableEtiqueta;
import com.contabilidad.cuentaafectable.service.CuentaAfectableService;
import com.contabilidad.esquema.dto.EsquemaContableRequest;
import com.contabilidad.esquema.dto.EsquemaContableResponse;
import com.contabilidad.esquema.dto.EsquemaDetalleRequest;
import com.contabilidad.esquema.entity.EsquemaContable;
import com.contabilidad.esquema.entity.EsquemaContableDetalle;
import com.contabilidad.esquema.repository.EsquemaContableDetalleRepository;
import com.contabilidad.esquema.repository.EsquemaContableRepository;
import com.contabilidad.motor.EvaluadorFormula;
import com.contabilidad.operacion.repository.OperacionContableRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class EsquemaContableService {

    @Inject
    AuditContext audit;

    @Inject
    EsquemaContableRepository repository;

    @Inject
    EsquemaContableDetalleRepository detalleRepository;

    @Inject
    CuentaAfectableService afectableService;

    @Inject
    OperacionContableRepository operacionRepository;

    public List<EsquemaContableResponse> listAll() {
        List<EsquemaContable> esquemas = repository.listOrdenados();
        Map<Long, List<EsquemaContableDetalle>> detalles = detalleRepository
                .agrupadosPorEsquema(esquemas.stream().map(EsquemaContable::getId).toList());
        Map<Long, AfectableEtiqueta> etiquetas = etiquetasDe(detalles.values().stream()
                .flatMap(List::stream).map(EsquemaContableDetalle::getAfectableId).collect(Collectors.toSet()));

        return esquemas.stream()
                .map(e -> armar(e, detalles.getOrDefault(e.getId(), List.of()), etiquetas))
                .toList();
    }

    public EsquemaContableResponse findById(Long id) {
        EsquemaContable e = findEntityOrThrow(id);
        List<EsquemaContableDetalle> detalle = detalleRepository.listByEsquema(id);
        return armar(e, detalle, etiquetasDe(detalle.stream()
                .map(EsquemaContableDetalle::getAfectableId).collect(Collectors.toSet())));
    }

    @Transactional
    public EsquemaContableResponse create(EsquemaContableRequest request) {
        validar(request, null);

        EsquemaContable e = new EsquemaContable();
        e.setCodigo(request.codigo());
        e.setNombre(request.nombre());
        e.setDescripcion(request.descripcion());
        e.setVersion(repository.maxVersion(request.codigo()) + 1);
        e.setEstado(request.estado());
        e.setFechaInicio(LocalDate.now());
        e.setUsuario(audit.actor());
        e.setEstacion(audit.estacion());
        repository.persist(e);
        repository.getEntityManager().flush();

        grabarDetalle(e.getId(), request.detalle());
        return findById(e.getId());
    }

    @Transactional
    public EsquemaContableResponse update(Long id, EsquemaContableRequest request) {
        EsquemaContable e = findEntityOrThrow(id);
        if (operacionRepository.existsPorEsquema(id)) {
            throw new WebApplicationException(
                    "El esquema " + e.getCodigo() + " v" + e.getVersion() + " ya generó asientos: "
                            + "no se edita. Creá una versión nueva.", Response.Status.CONFLICT);
        }
        validar(request, id);
        if (!request.codigo().equals(e.getCodigo())) {
            throw new BadRequestException("El código del esquema no se puede cambiar; creá otro.");
        }

        e.setNombre(request.nombre());
        e.setDescripcion(request.descripcion());
        e.setEstado(request.estado());
        e.setUsuario(audit.actor());
        e.setEstacion(audit.estacion());

        detalleRepository.deleteByEsquema(id);
        detalleRepository.getEntityManager().flush();
        grabarDetalle(id, request.detalle());
        return findById(id);
    }

    /**
     * Copia el esquema a la versión siguiente, en estado suspendido para que el
     * contador la revise y la active. Es la vía para cambiar una receta que ya
     * generó asientos sin tocar los históricos.
     */
    @Transactional
    public EsquemaContableResponse nuevaVersion(Long id) {
        EsquemaContable actual = findEntityOrThrow(id);

        EsquemaContable copia = new EsquemaContable();
        copia.setCodigo(actual.getCodigo());
        copia.setNombre(actual.getNombre());
        copia.setDescripcion(actual.getDescripcion());
        copia.setVersion(repository.maxVersion(actual.getCodigo()) + 1);
        copia.setEstado("S");
        copia.setFechaInicio(LocalDate.now());
        copia.setUsuario(audit.actor());
        copia.setEstacion(audit.estacion());
        repository.persist(copia);
        repository.getEntityManager().flush();

        int secuencia = 1;
        for (EsquemaContableDetalle d : detalleRepository.listByEsquema(id)) {
            EsquemaContableDetalle c = new EsquemaContableDetalle();
            c.setEsquemaContableId(copia.getId());
            c.setSecuencia(secuencia++);
            c.setDomDebeHaber(d.getDomDebeHaber());
            c.setAfectableId(d.getAfectableId());
            c.setFormula(d.getFormula());
            c.setDescripcion(d.getDescripcion());
            detalleRepository.persist(c);
        }
        detalleRepository.getEntityManager().flush();
        return findById(copia.getId());
    }

    public EsquemaContable findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Esquema contable no encontrado: " + id));
    }

    // ---- internos ----

    private void validar(EsquemaContableRequest request, Long idActual) {
        boolean hayDebe = request.detalle().stream().anyMatch(d -> "D".equals(d.domDebeHaber()));
        boolean hayHaber = request.detalle().stream().anyMatch(d -> "H".equals(d.domDebeHaber()));
        if (!hayDebe || !hayHaber) {
            throw new BadRequestException("El esquema necesita al menos un renglón al debe y otro al haber.");
        }

        int i = 1;
        for (EsquemaDetalleRequest d : request.detalle()) {
            afectableService.findVigenteOrThrow(d.afectableId());
            try {
                EvaluadorFormula.validarSintaxis(d.formula(), "renglón " + i);
            } catch (EvaluadorFormula.FormulaInvalidaException e) {
                throw new BadRequestException(e.getMessage());
            }
            i++;
        }

        if ("V".equals(request.estado())) {
            Integer version = idActual == null ? null
                    : repository.findByIdOptional(idActual).map(EsquemaContable::getVersion).orElse(null);
            if (repository.existsVigenteDistintaVersion(request.codigo(),
                    version == null ? -1 : version)) {
                throw new WebApplicationException(
                        "Ya hay otra versión vigente del esquema " + request.codigo()
                                + ". Suspendé esa antes de poner esta en vigencia.",
                        Response.Status.CONFLICT);
            }
        }
    }

    private void grabarDetalle(Long esquemaId, List<EsquemaDetalleRequest> detalle) {
        int secuencia = 1;
        for (EsquemaDetalleRequest req : detalle) {
            EsquemaContableDetalle d = new EsquemaContableDetalle();
            d.setEsquemaContableId(esquemaId);
            d.setSecuencia(secuencia++);
            d.setDomDebeHaber(req.domDebeHaber());
            d.setAfectableId(req.afectableId());
            d.setFormula(req.formula());
            d.setDescripcion(req.descripcion());
            detalleRepository.persist(d);
        }
        detalleRepository.getEntityManager().flush();
    }

    private Map<Long, AfectableEtiqueta> etiquetasDe(Set<Long> afectableIds) {
        return afectableService.etiquetas(afectableIds);
    }

    private EsquemaContableResponse armar(EsquemaContable e, List<EsquemaContableDetalle> detalle,
                                          Map<Long, AfectableEtiqueta> etiquetas) {
        List<EsquemaContableResponse.Detalle> filas = new ArrayList<>();
        for (EsquemaContableDetalle d : detalle) {
            AfectableEtiqueta et = etiquetas.get(d.getAfectableId());
            filas.add(EsquemaContableResponse.Detalle.from(d,
                    et == null ? null : et.codigo(),
                    et == null ? null : et.cuentaNombre()));
        }
        return EsquemaContableResponse.from(e, operacionRepository.existsPorEsquema(e.getId()), filas);
    }
}
