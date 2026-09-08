package com.bien.common.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * Devuelve el mensaje de las {@code WebApplicationException} en el cuerpo.
 * <p>
 * Por defecto Quarkus responde el status correcto pero con cuerpo vacío, así que
 * el cliente solo veía "Request failed with status code 409" y se perdía el
 * motivo real ("El socio ya tiene una solicitud de retiro abierta", "Hay períodos
 * anteriores sin cerrar", etc.). Los servicios ya lanzan estas excepciones con un
 * mensaje pensado para el usuario; esto solo lo deja llegar.
 * <p>
 * No afecta a los errores de bean validation: {@code ResteasyReactiveViolationException}
 * es una subclase y JAX-RS elige el mapper más específico, así que esos siguen
 * saliendo con su formato {@code violations}.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();

        // si alguien ya construyó un cuerpo a mano, respetarlo
        if (original.hasEntity()) {
            return original;
        }

        int status = original.getStatus();
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = original.getStatusInfo().getReasonPhrase();
        }

        return Response.status(status)
                .entity(Map.of("message", message, "status", status))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
