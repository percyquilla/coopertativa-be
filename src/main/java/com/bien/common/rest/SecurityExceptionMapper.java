package com.bien.common.rest;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * Cuerpo en español para las denegaciones de seguridad de Quarkus.
 * <p>
 * {@code io.quarkus.security.ForbiddenException} / {@code UnauthorizedException}
 * NO son {@code WebApplicationException}, así que no las toma
 * {@link WebApplicationExceptionMapper}: Quarkus respondía 401/403 con cuerpo
 * vacío y el frontend solo mostraba "Request failed with status code 403". Con
 * el modelo de roles por módulo un usuario puede toparse con un 403
 * legítimamente (un cajero entrando a contabilidad) y merece un mensaje claro.
 */
public final class SecurityExceptionMapper {

    private SecurityExceptionMapper() {
    }

    private static Response body(Response.Status status, String message) {
        return Response.status(status)
                .entity(Map.of("message", message, "status", status.getStatusCode()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Provider
    public static class Forbidden implements ExceptionMapper<ForbiddenException> {
        @Override
        public Response toResponse(ForbiddenException exception) {
            return body(Response.Status.FORBIDDEN, "Tu rol no tiene permiso para esta operación.");
        }
    }

    @Provider
    public static class Unauthorized implements ExceptionMapper<UnauthorizedException> {
        @Override
        public Response toResponse(UnauthorizedException exception) {
            return body(Response.Status.UNAUTHORIZED, "Necesitás iniciar sesión para hacer esto.");
        }
    }
}
