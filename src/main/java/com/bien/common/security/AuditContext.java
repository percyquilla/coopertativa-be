package com.bien.common.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

/**
 * Quién y desde dónde se ejecuta la petición actual, para estampar las columnas
 * de auditoría ({@code usuario}, {@code estacion}) de cada registro.
 * <p>
 * El nombre sale del JWT (claim {@code upn}), no del cuerpo del request: antes
 * los formularios mandaban este dato y casi siempre era {@code null}, así que no
 * había traza de quién hizo cada operación. Ahora los servicios lo toman de aquí
 * e ignoran lo que venga en el DTO.
 * <p>
 * La estación es la IP del cliente (Observaciones #2). Detrás de un proxy o
 * balanceador (Cloud Run) la IP real llega en {@code X-Forwarded-For}; si no
 * hay request HTTP (tareas internas) se usa {@code "SISTEMA"}.
 */
@RequestScoped
public class AuditContext {

    static final String SISTEMA = "SISTEMA";

    /** Largo de la columna {@code estacion} en todas las tablas. */
    private static final int MAX_ESTACION = 30;

    @Inject
    SecurityIdentity identity;

    @Inject
    CurrentVertxRequest currentRequest;

    /** Username del usuario autenticado, o {@code "SISTEMA"} si no hay identidad. */
    public String actor() {
        if (identity == null || identity.isAnonymous() || identity.getPrincipal() == null) {
            return SISTEMA;
        }
        String name = identity.getPrincipal().getName();
        return (name == null || name.isBlank()) ? SISTEMA : name;
    }

    /** IP de la estación que origina la petición, o {@code "SISTEMA"} si no hay request. */
    public String estacion() {
        RoutingContext ctx = currentRequest == null ? null : currentRequest.getCurrent();
        if (ctx == null) {
            return SISTEMA;
        }
        HttpServerRequest request = ctx.request();
        String ip = primeraIp(request.getHeader("X-Forwarded-For"));
        if (ip == null) {
            ip = primeraIp(request.getHeader("X-Real-IP"));
        }
        if (ip == null) {
            SocketAddress remote = request.remoteAddress();
            ip = remote == null ? null : remote.hostAddress();
        }
        if (ip == null || ip.isBlank()) {
            return SISTEMA;
        }
        return ip.length() > MAX_ESTACION ? ip.substring(0, MAX_ESTACION) : ip;
    }

    /** {@code X-Forwarded-For} puede traer "cliente, proxy1, proxy2": el cliente es el primero. */
    private static String primeraIp(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String first = header.split(",")[0].trim();
        return first.isEmpty() ? null : first;
    }
}
