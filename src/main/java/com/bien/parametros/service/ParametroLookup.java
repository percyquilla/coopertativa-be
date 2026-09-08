package com.bien.parametros.service;

import com.bien.parametros.entity.ParametroGeneral;
import com.bien.parametros.repository.ParametroGeneralRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Lectura tipada de {@code sc_parametros.parametro_general} para el resto del
 * backend. Antes estos parámetros eran editables desde la pantalla pero ningún
 * servicio los consultaba (el histórico de contraseñas estaba fijo en 5 en el
 * código, el bloqueo por intentos nunca se aplicaba). Cada consulta cae a un
 * valor por defecto si el parámetro no existe o está mal cargado.
 */
@ApplicationScoped
public class ParametroLookup {

    @Inject
    ParametroGeneralRepository repository;

    public int intValue(String codigo, int porDefecto) {
        return repository.findByCodigo(codigo)
                .map(ParametroGeneral::getValor)
                .map(v -> {
                    try {
                        return Integer.parseInt(v.trim());
                    } catch (NumberFormatException e) {
                        return porDefecto;
                    }
                })
                .orElse(porDefecto);
    }

    public boolean boolValue(String codigo, boolean porDefecto) {
        return repository.findByCodigo(codigo)
                .map(ParametroGeneral::getValor)
                .map(v -> {
                    String t = v.trim().toLowerCase();
                    if (t.equals("true") || t.equals("1") || t.equals("si") || t.equals("sí")) {
                        return true;
                    }
                    if (t.equals("false") || t.equals("0") || t.equals("no")) {
                        return false;
                    }
                    return porDefecto;
                })
                .orElse(porDefecto);
    }
}
