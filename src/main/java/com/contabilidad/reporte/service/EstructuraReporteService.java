package com.contabilidad.reporte.service;

import com.bien.common.security.AuditContext;
import com.contabilidad.cuentacontable.entity.CuentaContable;
import com.contabilidad.cuentacontable.repository.CuentaContableRepository;
import com.contabilidad.motor.EvaluadorFormula;
import com.contabilidad.reporte.dto.EstructuraReporteResumen;
import com.contabilidad.reporte.dto.ReporteLineaRequest;
import com.contabilidad.reporte.dto.ReporteLineaResponse;
import com.contabilidad.reporte.entity.EstructuraReporte;
import com.contabilidad.reporte.entity.EstructuraReporteCuenta;
import com.contabilidad.reporte.entity.EstructuraReporteLinea;
import com.contabilidad.reporte.repository.EstructuraReporteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ABM de la estructura de los reportes financieros (líneas del ESF / ER y su
 * mapeo a cuentas). El armado del reporte con números vive en {@link ReporteService};
 * acá solo se edita la plantilla.
 */
@ApplicationScoped
public class EstructuraReporteService {

    private static final Set<String> CON_FORMULA = Set.of("SUBTOTAL", "TOTAL");

    @Inject
    AuditContext audit;

    @Inject
    EstructuraReporteRepository repository;

    @Inject
    CuentaContableRepository cuentaRepository;

    public List<EstructuraReporteResumen> listarReportes() {
        return repository.listOrdenados().stream()
                .map(e -> new EstructuraReporteResumen(e.getId(), e.getCodigo(), e.getNombre(),
                        e.getTipo(), e.getEstado()))
                .toList();
    }

    public List<ReporteLineaResponse> lineasDe(String codigoReporte) {
        EstructuraReporte estructura = requireEstructura(codigoReporte);
        List<EstructuraReporteLinea> lineas = repository.lineasDe(estructura.getId());

        Map<Long, List<EstructuraReporteCuenta>> cuentasPorLinea = repository
                .cuentasDe(lineas.stream().map(EstructuraReporteLinea::getId).toList())
                .stream().collect(Collectors.groupingBy(EstructuraReporteCuenta::getEstructuraReporteLineaId));

        Map<Long, CuentaContable> cuentas = indexCuentas(cuentasPorLinea.values().stream()
                .flatMap(List::stream).map(EstructuraReporteCuenta::getCuentaId).collect(Collectors.toSet()));

        List<ReporteLineaResponse> salida = new ArrayList<>();
        for (EstructuraReporteLinea l : lineas) {
            List<ReporteLineaResponse.Cuenta> maps = cuentasPorLinea.getOrDefault(l.getId(), List.of()).stream()
                    .map(m -> {
                        CuentaContable c = cuentas.get(m.getCuentaId());
                        return new ReporteLineaResponse.Cuenta(m.getId(), m.getCuentaId(),
                                c == null ? null : c.getCodigo(), c == null ? null : c.getNombre(), m.getSigno());
                    })
                    .sorted((a, b) -> nullSafe(a.cuentaCodigo()).compareTo(nullSafe(b.cuentaCodigo())))
                    .toList();
            salida.add(toResponse(l, maps));
        }
        return salida;
    }

    @Transactional
    public ReporteLineaResponse crear(String codigoReporte, ReporteLineaRequest req) {
        EstructuraReporte estructura = requireEstructura(codigoReporte);
        validar(estructura.getId(), null, req);

        EstructuraReporteLinea l = new EstructuraReporteLinea();
        l.setEstructuraReporteId(estructura.getId());
        aplicar(l, req);
        repository.persistLinea(l);
        repository.flush();

        grabarCuentas(l, req);
        repository.flush();
        return releer(l.getId());
    }

    @Transactional
    public ReporteLineaResponse actualizar(Long lineaId, ReporteLineaRequest req) {
        EstructuraReporteLinea l = repository.lineaById(lineaId)
                .orElseThrow(() -> new NotFoundException("Línea de reporte no encontrada: " + lineaId));
        validar(l.getEstructuraReporteId(), l, req);

        // si le cambian el código, no puede estar en uso por la fórmula de otra línea
        if (!l.getCodigo().equals(req.codigo())) {
            requireCodigoNoReferenciado(l.getEstructuraReporteId(), l.getCodigo(), l.getId(),
                    "renombrar la línea «" + l.getCodigo() + "»");
        }

        aplicar(l, req);
        repository.borrarCuentasDeLinea(l.getId());
        grabarCuentas(l, req);
        repository.flush();
        return releer(l.getId());
    }

    @Transactional
    public void eliminar(Long lineaId) {
        EstructuraReporteLinea l = repository.lineaById(lineaId)
                .orElseThrow(() -> new NotFoundException("Línea de reporte no encontrada: " + lineaId));
        if (repository.tieneHijas(l.getId())) {
            throw new WebApplicationException("La línea «" + l.getCodigo()
                    + "» es padre de otras líneas: reasignalas antes de borrarla.", Response.Status.CONFLICT);
        }
        requireCodigoNoReferenciado(l.getEstructuraReporteId(), l.getCodigo(), l.getId(),
                "borrar la línea «" + l.getCodigo() + "»");
        repository.borrarCuentasDeLinea(l.getId());
        repository.borrarLinea(l);
    }

    // ---- internos ----

    private void validar(Long estructuraReporteId, EstructuraReporteLinea actual, ReporteLineaRequest req) {
        if (req.signo() != 1 && req.signo() != -1) {
            throw new BadRequestException("El signo de la línea debe ser 1 o -1.");
        }
        if (repository.existeCodigo(estructuraReporteId, req.codigo(), actual == null ? null : actual.getId())) {
            throw new WebApplicationException("Ya hay una línea con el código «" + req.codigo()
                    + "» en este reporte.", Response.Status.CONFLICT);
        }
        if (req.lineaPadreId() != null) {
            EstructuraReporteLinea padre = repository.lineaById(req.lineaPadreId())
                    .orElseThrow(() -> new BadRequestException("La línea padre indicada no existe."));
            if (!padre.getEstructuraReporteId().equals(estructuraReporteId)) {
                throw new BadRequestException("La línea padre pertenece a otro reporte.");
            }
            if (actual != null && padre.getId().equals(actual.getId())) {
                throw new BadRequestException("Una línea no puede ser su propia línea padre.");
            }
        }

        boolean conFormula = CON_FORMULA.contains(req.tipoLinea());
        if (conFormula) {
            if (req.formula() == null || req.formula().isBlank()) {
                throw new BadRequestException("Una línea " + req.tipoLinea()
                        + " necesita una fórmula sobre los códigos de otras líneas.");
            }
            try {
                EvaluadorFormula.validarSintaxis(req.formula(), "la fórmula de «" + req.codigo() + "»");
            } catch (EvaluadorFormula.FormulaInvalidaException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

        List<ReporteLineaRequest.Cuenta> cuentas = req.cuentas() == null ? List.of() : req.cuentas();
        if (!"DETALLE".equals(req.tipoLinea()) && !cuentas.isEmpty()) {
            throw new BadRequestException("Solo las líneas DETALLE pueden mapear cuentas.");
        }
        if (!cuentas.isEmpty()) {
            for (ReporteLineaRequest.Cuenta c : cuentas) {
                if (c.signo() != 1 && c.signo() != -1) {
                    throw new BadRequestException("El signo de cada cuenta debe ser 1 o -1.");
                }
            }
            Set<Long> ids = cuentas.stream().map(ReporteLineaRequest.Cuenta::cuentaId).collect(Collectors.toSet());
            if (ids.size() != cuentas.size()) {
                throw new BadRequestException("Hay una cuenta repetida en el mapeo.");
            }
            Set<Long> existentes = cuentaRepository.listByIds(ids).stream()
                    .map(CuentaContable::getId).collect(Collectors.toSet());
            ids.removeAll(existentes);
            if (!ids.isEmpty()) {
                throw new NotFoundException("No existe la cuenta contable con id " + ids.iterator().next() + ".");
            }
        }
    }

    private void requireCodigoNoReferenciado(Long estructuraReporteId, String codigo, Long exceptoLineaId,
            String accion) {
        Pattern token = Pattern.compile("\\b" + Pattern.quote(codigo) + "\\b");
        List<String> usan = repository.lineasDe(estructuraReporteId).stream()
                .filter(o -> !o.getId().equals(exceptoLineaId))
                .filter(o -> o.getFormula() != null && token.matcher(o.getFormula()).find())
                .map(EstructuraReporteLinea::getCodigo)
                .toList();
        if (!usan.isEmpty()) {
            throw new WebApplicationException("No se puede " + accion + ": su código lo usan las fórmulas de "
                    + String.join(", ", usan) + ".", Response.Status.CONFLICT);
        }
    }

    private void aplicar(EstructuraReporteLinea l, ReporteLineaRequest req) {
        boolean conFormula = CON_FORMULA.contains(req.tipoLinea());
        l.setLineaPadreId(req.lineaPadreId());
        l.setCodigo(req.codigo());
        l.setTitulo(req.titulo());
        l.setOrden(req.orden());
        l.setTipoLinea(req.tipoLinea());
        l.setSigno(req.signo());
        l.setFormula(conFormula ? req.formula().trim() : null);
        l.setEstado(req.estado() == null || req.estado().isBlank() ? "V" : req.estado());
        l.setUsuario(audit.actor());
        l.setEstacion(audit.estacion());
    }

    private void grabarCuentas(EstructuraReporteLinea l, ReporteLineaRequest req) {
        if (!"DETALLE".equals(req.tipoLinea()) || req.cuentas() == null) {
            return;
        }
        for (ReporteLineaRequest.Cuenta c : req.cuentas()) {
            EstructuraReporteCuenta m = new EstructuraReporteCuenta();
            m.setEstructuraReporteLineaId(l.getId());
            m.setCuentaId(c.cuentaId());
            m.setSigno(c.signo());
            m.setUsuario(audit.actor());
            m.setEstacion(audit.estacion());
            repository.persistCuenta(m);
        }
    }

    private ReporteLineaResponse releer(Long lineaId) {
        EstructuraReporteLinea l = repository.lineaById(lineaId).orElseThrow();
        Map<Long, CuentaContable> cuentas = indexCuentas(repository.cuentasDeLinea(lineaId).stream()
                .map(EstructuraReporteCuenta::getCuentaId).collect(Collectors.toSet()));
        List<ReporteLineaResponse.Cuenta> maps = repository.cuentasDeLinea(lineaId).stream()
                .map(m -> {
                    CuentaContable c = cuentas.get(m.getCuentaId());
                    return new ReporteLineaResponse.Cuenta(m.getId(), m.getCuentaId(),
                            c == null ? null : c.getCodigo(), c == null ? null : c.getNombre(), m.getSigno());
                })
                .sorted((a, b) -> nullSafe(a.cuentaCodigo()).compareTo(nullSafe(b.cuentaCodigo())))
                .toList();
        return toResponse(l, maps);
    }

    private Map<Long, CuentaContable> indexCuentas(Set<Long> ids) {
        return ids.isEmpty() ? Map.of()
                : cuentaRepository.listByIds(ids).stream()
                        .collect(Collectors.toMap(CuentaContable::getId, Function.identity()));
    }

    private EstructuraReporte requireEstructura(String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new NotFoundException("Estructura de reporte no encontrada: " + codigo));
    }

    private static ReporteLineaResponse toResponse(EstructuraReporteLinea l, List<ReporteLineaResponse.Cuenta> maps) {
        return new ReporteLineaResponse(l.getId(), l.getEstructuraReporteId(), l.getLineaPadreId(),
                l.getCodigo(), l.getTitulo(), l.getOrden(), l.getTipoLinea(), l.getSigno(),
                l.getFormula(), l.getEstado(), maps);
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
