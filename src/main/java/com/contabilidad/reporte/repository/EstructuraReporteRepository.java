package com.contabilidad.reporte.repository;

import com.contabilidad.reporte.entity.EstructuraReporte;
import com.contabilidad.reporte.entity.EstructuraReporteCuenta;
import com.contabilidad.reporte.entity.EstructuraReporteLinea;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Una sola clase para las tres tablas de estructura: se consultan siempre juntas
 * para armar un reporte y para el ABM de líneas.
 */
@ApplicationScoped
public class EstructuraReporteRepository implements PanacheRepositoryBase<EstructuraReporte, Long> {

    @Inject
    LineaRepo lineaRepo;

    @Inject
    CuentaRepo cuentaRepo;

    public Optional<EstructuraReporte> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }

    public List<EstructuraReporte> listOrdenados() {
        return list("order by codigo");
    }

    public List<EstructuraReporteLinea> lineasDe(Long estructuraReporteId) {
        return lineaRepo.list("estructuraReporteId = ?1 order by orden", estructuraReporteId);
    }

    public List<EstructuraReporteCuenta> cuentasDe(List<Long> lineaIds) {
        return lineaIds.isEmpty() ? List.of()
                : cuentaRepo.list("estructuraReporteLineaId in ?1", lineaIds);
    }

    // ---- ABM de líneas ----

    public Optional<EstructuraReporteLinea> lineaById(Long id) {
        return lineaRepo.findByIdOptional(id);
    }

    public boolean existeCodigo(Long estructuraReporteId, String codigo, Long exceptoLineaId) {
        return lineaRepo.count("estructuraReporteId = ?1 and codigo = ?2 and id <> ?3",
                estructuraReporteId, codigo, exceptoLineaId == null ? -1L : exceptoLineaId) > 0;
    }

    public boolean tieneHijas(Long lineaId) {
        return lineaRepo.count("lineaPadreId = ?1", lineaId) > 0;
    }

    public List<EstructuraReporteCuenta> cuentasDeLinea(Long lineaId) {
        return cuentaRepo.list("estructuraReporteLineaId = ?1", lineaId);
    }

    public void borrarCuentasDeLinea(Long lineaId) {
        cuentaRepo.delete("estructuraReporteLineaId = ?1", lineaId);
    }

    public void persistLinea(EstructuraReporteLinea linea) {
        lineaRepo.persist(linea);
    }

    public void persistCuenta(EstructuraReporteCuenta cuenta) {
        cuentaRepo.persist(cuenta);
    }

    public void borrarLinea(EstructuraReporteLinea linea) {
        lineaRepo.delete(linea);
    }

    public void flush() {
        lineaRepo.getEntityManager().flush();
    }

    @ApplicationScoped
    public static class LineaRepo implements PanacheRepositoryBase<EstructuraReporteLinea, Long> {
    }

    @ApplicationScoped
    public static class CuentaRepo implements PanacheRepositoryBase<EstructuraReporteCuenta, Long> {
    }
}
