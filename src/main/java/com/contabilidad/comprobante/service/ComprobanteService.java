package com.contabilidad.comprobante.service;

import com.bien.common.security.AuditContext;
import com.bien.parametros.entity.Institucion;
import com.bien.parametros.repository.InstitucionRepository;
import com.contabilidad.comprobante.dto.ComprobanteRengRequest;
import com.contabilidad.comprobante.dto.ComprobanteRengResponse;
import com.contabilidad.comprobante.dto.ComprobanteRequest;
import com.contabilidad.comprobante.dto.ComprobanteResponse;
import com.contabilidad.comprobante.entity.Comprobante;
import com.contabilidad.comprobante.entity.ComprobanteReng;
import com.contabilidad.comprobante.repository.ComprobanteRengRepository;
import com.contabilidad.comprobante.repository.ComprobanteRepository;
import com.contabilidad.cuentaafectable.dto.AfectableEtiqueta;
import com.contabilidad.cuentaafectable.service.CuentaAfectableService;
import com.contabilidad.periodocontable.entity.PeriodoContable;
import com.contabilidad.periodocontable.service.PeriodoContableService;
import com.contabilidad.saldo.service.SaldoCuentaService;
import com.contabilidad.tipocomprobante.entity.TipoComprobante;
import com.contabilidad.tipocomprobante.service.TipoComprobanteService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Libro diario. Un comprobante nace en borrador (REG), se edita libremente y al
 * autorizarse (AUT) recibe su número correlativo y queda inmutable.
 * <p>
 * La partida doble se valida acá, con un mensaje que el contador entienda; el
 * trigger {@code tr_comprobante_partida_doble} la vuelve a validar en la base y
 * es el que garantiza que nada entre descuadrado, venga de donde venga.
 */
@ApplicationScoped
public class ComprobanteService {

    @Inject
    AuditContext audit;

    @Inject
    ComprobanteRepository repository;

    @Inject
    ComprobanteRengRepository rengRepository;

    @Inject
    TipoComprobanteService tipoService;

    @Inject
    PeriodoContableService periodoService;

    @Inject
    CuentaAfectableService afectableService;

    @Inject
    InstitucionRepository institucionRepository;

    @Inject
    SaldoCuentaService saldoService;

    @Inject
    com.contabilidad.diacontable.service.DiaContableService diaService;

    /**
     * Listado. No trae los renglones (serían cientos por página); sí los totales,
     * que es lo que la pantalla necesita mostrar. El detalle va por
     * {@link #findById(Long)}.
     */
    public List<ComprobanteResponse> listAll(Long periodoId, Long tipoComprobanteId, String estado,
                                             LocalDate desde, LocalDate hasta) {
        List<Comprobante> comprobantes = repository.listFiltrado(periodoId, tipoComprobanteId, estado, desde, hasta);
        Map<Long, List<ComprobanteReng>> renglones = rengRepository.agrupadosPorComprobante(
                comprobantes.stream().map(Comprobante::getId).toList());
        Map<Long, TipoComprobante> tipos = tiposPorId(comprobantes);

        return comprobantes.stream()
                .map(c -> armar(c, tipos, renglones.getOrDefault(c.getId(), List.of()), Map.of()))
                .toList();
    }

    public ComprobanteResponse findById(Long id) {
        Comprobante c = findEntityOrThrow(id);
        List<ComprobanteReng> renglones = rengRepository.listByComprobante(id);
        return armar(c, tiposPorId(List.of(c)), renglones, etiquetasDe(renglones));
    }

    @Transactional
    public ComprobanteResponse create(ComprobanteRequest request) {
        TipoComprobante tipo = tipoService.findVigenteOrThrow(request.tipoComprobanteId());
        PeriodoContable periodo = periodoService.findAbiertoPorFechaOrThrow(request.fecha());
        diaService.requireDiaAbierto(request.fecha());
        validarRenglones(request.renglones());

        Comprobante c = new Comprobante();
        c.setPeriodoId(periodo.getId());
        c.setTipoComprobanteId(tipo.getId());
        c.setFecha(request.fecha().atStartOfDay());
        c.setGlosa(request.glosa());
        c.setEstado("REG");
        c.setUsuarioCreacion(audit.actor());
        c.setEstacion("SISTEMA");
        repository.persist(c);
        repository.getEntityManager().flush();

        grabarRenglones(c, request.renglones());
        repository.getEntityManager().refresh(c);
        return findById(c.getId());
    }

    @Transactional
    public ComprobanteResponse update(Long id, ComprobanteRequest request) {
        Comprobante c = findEntityOrThrow(id);
        requireBorrador(c, "editar");

        TipoComprobante tipo = tipoService.findVigenteOrThrow(request.tipoComprobanteId());
        PeriodoContable periodo = periodoService.findAbiertoPorFechaOrThrow(request.fecha());
        diaService.requireDiaAbierto(request.fecha());
        validarRenglones(request.renglones());

        c.setTipoComprobanteId(tipo.getId());
        c.setPeriodoId(periodo.getId());
        c.setFecha(request.fecha().atStartOfDay());
        c.setGlosa(request.glosa());
        c.setUsuarioCreacion(audit.actor());

        // se reemplazan enteros: renumerar renglones sueltos daría más código que rehacerlos
        rengRepository.deleteByComprobante(id);
        rengRepository.getEntityManager().flush();
        grabarRenglones(c, request.renglones());

        return findById(id);
    }

    /**
     * Autoriza el asiento: le asigna el correlativo de su serie y lo congela. Es
     * el momento en que el comprobante empieza a existir para la contabilidad.
     */
    @Transactional
    public ComprobanteResponse autorizar(Long id) {
        Comprobante c = findEntityOrThrow(id);
        requireBorrador(c, "autorizar");
        requirePeriodoAbierto(c);
        diaService.requireDiaAbierto(c.getFecha().toLocalDate());

        List<ComprobanteReng> renglones = rengRepository.listByComprobante(id);
        if (renglones.size() < 2) {
            throw new BadRequestException("Un asiento necesita al menos dos renglones.");
        }
        verificarCuadre(total(renglones, true), total(renglones, false));

        Long numero = repository.siguienteNumero(c.getTipoComprobanteId(), c.getPeriodoId());
        c.setNumero(numero);
        c.setEstado("AUT");
        c.setUsuarioAutoriz(audit.actor());
        c.setFechaAutoriz(LocalDateTime.now());
        repository.getEntityManager().flush();

        saldoService.aplicar(c, renglones);
        return findById(id);
    }

    /**
     * Anula el comprobante. Un borrador simplemente pasa a ANL; uno autorizado no
     * se toca — se emite un asiento de reversión con los lados invertidos, fechado
     * hoy, y los dos quedan enlazados. Es la única forma de corregir sin romper la
     * secuencia ni la historia.
     */
    @Transactional
    public ComprobanteResponse anular(Long id, String motivo) {
        Comprobante c = findEntityOrThrow(id);

        if ("ANL".equals(c.getEstado())) {
            throw new WebApplicationException("El comprobante ya está anulado.", Response.Status.CONFLICT);
        }
        if (c.esBorrador()) {
            c.setEstado("ANL");
            c.setUsuarioCreacion(audit.actor());
            return findById(id);
        }
        if (c.getComprobanteReversionId() != null) {
            throw new WebApplicationException(
                    "El comprobante ya fue revertido por el comprobante " + c.getComprobanteReversionId() + ".",
                    Response.Status.CONFLICT);
        }

        PeriodoContable periodo = periodoService.findAbiertoPorFechaOrThrow(LocalDate.now());
        diaService.requireDiaAbierto(LocalDate.now());
        List<ComprobanteReng> originales = rengRepository.listByComprobante(id);

        Comprobante rev = new Comprobante();
        rev.setPeriodoId(periodo.getId());
        rev.setTipoComprobanteId(c.getTipoComprobanteId());
        rev.setFecha(LocalDate.now().atStartOfDay());
        rev.setGlosa(glosaDeReversion(c, motivo));
        rev.setEstado("REG");
        rev.setUsuarioCreacion(audit.actor());
        rev.setEstacion("SISTEMA");
        repository.persist(rev);
        repository.getEntityManager().flush();

        int secuencia = 1;
        for (ComprobanteReng original : originales) {
            ComprobanteReng r = new ComprobanteReng();
            r.setComprobanteId(rev.getId());
            r.setSecuencia(secuencia++);
            r.setAfectableId(original.getAfectableId());
            r.setDomDebeHaber(original.esDebe() ? "H" : "D");
            r.setMonto(original.getMonto());
            r.setMonedaIdMo(original.getMonedaIdMo());
            r.setMontoMo(original.getMontoMo());
            r.setTipoCambio(original.getTipoCambio());
            r.setGlosa(original.getGlosa());
            rengRepository.persist(r);
        }
        rengRepository.getEntityManager().flush();

        rev.setNumero(repository.siguienteNumero(rev.getTipoComprobanteId(), rev.getPeriodoId()));
        rev.setEstado("AUT");
        rev.setUsuarioAutoriz(audit.actor());
        rev.setFechaAutoriz(LocalDateTime.now());
        c.setComprobanteReversionId(rev.getId());
        repository.getEntityManager().flush();
        repository.getEntityManager().refresh(rev);

        saldoService.aplicar(rev, rengRepository.listByComprobante(rev.getId()));
        return findById(rev.getId());
    }

    public Comprobante findEntityOrThrow(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Comprobante no encontrado: " + id));
    }

    // ---- internos ----

    private void requireBorrador(Comprobante c, String accion) {
        if (c.esBorrador()) {
            return;
        }
        String detalle;
        if (!c.estaAutorizado()) {
            detalle = "El comprobante está anulado y no se puede " + accion + ".";
        } else if ("autorizar".equals(accion)) {
            detalle = "El comprobante ya está autorizado con el número " + c.getNumero() + ".";
        } else {
            detalle = "Un comprobante autorizado no se puede " + accion
                    + ": anulalo para generar su comprobante de reversión.";
        }
        throw new WebApplicationException(detalle, Response.Status.CONFLICT);
    }

    /** El período pudo cerrarse entre el alta del borrador y su autorización. */
    private void requirePeriodoAbierto(Comprobante c) {
        PeriodoContable periodo = periodoService.findEntityOrThrow(c.getPeriodoId());
        if ("C".equals(periodo.getEstado())) {
            throw new WebApplicationException(
                    "El período " + periodo.getGestion() + "-" + periodo.getMes()
                            + " está cerrado: no se pueden autorizar comprobantes en él.",
                    Response.Status.CONFLICT);
        }
    }

    private void validarRenglones(List<ComprobanteRengRequest> renglones) {
        renglones.forEach(r -> afectableService.findVigenteOrThrow(r.afectableId()));

        BigDecimal debe = renglones.stream()
                .filter(r -> "D".equals(r.domDebeHaber()))
                .map(ComprobanteRengRequest::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal haber = renglones.stream()
                .filter(r -> "H".equals(r.domDebeHaber()))
                .map(ComprobanteRengRequest::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        verificarCuadre(debe, haber);
    }

    private void verificarCuadre(BigDecimal debe, BigDecimal haber) {
        if (debe.compareTo(haber) != 0) {
            throw new BadRequestException("El asiento no cuadra: el debe suma " + debe.toPlainString()
                    + " y el haber " + haber.toPlainString() + ".");
        }
        if (debe.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException("El asiento suma cero: no hay nada que contabilizar.");
        }
    }

    private void grabarRenglones(Comprobante c, List<ComprobanteRengRequest> renglones) {
        Long monedaBase = monedaBaseOrThrow();
        int secuencia = 1;
        for (ComprobanteRengRequest req : renglones) {
            ComprobanteReng r = new ComprobanteReng();
            r.setComprobanteId(c.getId());
            r.setSecuencia(secuencia++);
            r.setAfectableId(req.afectableId());
            r.setDomDebeHaber(req.domDebeHaber());
            r.setMonto(req.monto());
            // los asientos manuales se cargan en moneda base; el par monto/tipo de
            // cambio queda listo para cuando entren operaciones en otra moneda
            r.setMonedaIdMo(monedaBase);
            r.setMontoMo(req.monto());
            r.setTipoCambio(BigDecimal.ONE);
            r.setGlosa(req.glosa());
            rengRepository.persist(r);
        }
        rengRepository.getEntityManager().flush();
    }

    private Long monedaBaseOrThrow() {
        Long monedaBase = institucionRepository.findSingle().map(Institucion::getMonedaBaseId).orElse(null);
        if (monedaBase == null) {
            throw new WebApplicationException(
                    "La institución no tiene moneda base configurada: no se pueden registrar asientos.",
                    Response.Status.CONFLICT);
        }
        return monedaBase;
    }

    private String glosaDeReversion(Comprobante original, String motivo) {
        String base = "Reversión del comprobante " + original.getId()
                + (original.getNumero() == null ? "" : " (nº " + original.getNumero() + ")");
        String texto = motivo == null || motivo.isBlank() ? base : base + ": " + motivo;
        return texto.length() > 300 ? texto.substring(0, 300) : texto;
    }

    private BigDecimal total(List<ComprobanteReng> renglones, boolean debe) {
        return renglones.stream()
                .filter(r -> r.esDebe() == debe)
                .map(ComprobanteReng::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, TipoComprobante> tiposPorId(Collection<Comprobante> comprobantes) {
        return comprobantes.stream()
                .map(Comprobante::getTipoComprobanteId)
                .distinct()
                .map(tipoService::findEntityOrThrow)
                .collect(Collectors.toMap(TipoComprobante::getId, t -> t));
    }

    private Map<Long, AfectableEtiqueta> etiquetasDe(List<ComprobanteReng> renglones) {
        Set<Long> ids = renglones.stream().map(ComprobanteReng::getAfectableId).collect(Collectors.toSet());
        return afectableService.etiquetas(ids);
    }

    private ComprobanteResponse armar(Comprobante c, Map<Long, TipoComprobante> tipos,
                                      List<ComprobanteReng> renglones,
                                      Map<Long, AfectableEtiqueta> etiquetas) {
        TipoComprobante tipo = tipos.get(c.getTipoComprobanteId());

        List<ComprobanteRengResponse> detalle = new ArrayList<>();
        if (!etiquetas.isEmpty()) {
            for (ComprobanteReng r : renglones) {
                AfectableEtiqueta e = etiquetas.get(r.getAfectableId());
                detalle.add(ComprobanteRengResponse.from(r,
                        e == null ? null : e.codigo(),
                        e == null ? null : e.cuentaNombre()));
            }
        }

        return ComprobanteResponse.from(c,
                tipo == null ? null : tipo.getCodigo(),
                tipo == null ? null : tipo.getNombre(),
                total(renglones, true),
                total(renglones, false),
                detalle);
    }
}
