package com.bien.personaficha.dto;

import com.bien.personaficha.entity.PersonaContacto;
import com.bien.personaficha.entity.PersonaDireccion;
import com.bien.personaficha.entity.PersonaDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Request/response de los tres satélites de la ficha de una persona. */
public final class PersonaFichaDtos {

    private PersonaFichaDtos() {}

    // ---------- direcciones ----------

    public record DireccionRequest(
            @NotBlank
            @Pattern(regexp = "DOMICILIO|TRABAJO|CORRESPONDENCIA",
                    message = "El tipo de dirección debe ser DOMICILIO, TRABAJO o CORRESPONDENCIA.")
            String tipo,
            @Size(max = 60) String departamento,
            @Size(max = 60) String ciudad,
            @Size(max = 80) String zona,
            @NotBlank @Size(max = 250) String direccion,
            @Size(max = 250) String referencia,
            boolean esPrincipal,
            @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (dada de baja).")
            String estado) {
    }

    public record DireccionResponse(
            Long id, Long personaId, String tipo, String departamento, String ciudad, String zona,
            String direccion, String referencia, boolean esPrincipal, String estado) {

        public static DireccionResponse from(PersonaDireccion d) {
            return new DireccionResponse(d.getId(), d.getPersonaId(), d.getTipo(),
                    d.getDepartamento(), d.getCiudad(), d.getZona(), d.getDireccion(),
                    d.getReferencia(), d.isEsPrincipal(), d.getEstado());
        }
    }

    // ---------- contactos ----------

    public record ContactoRequest(
            @NotBlank
            @Pattern(regexp = "TELEFONO|CELULAR|EMAIL|OTRO",
                    message = "El tipo de contacto debe ser TELEFONO, CELULAR, EMAIL u OTRO.")
            String tipo,
            @NotBlank @Size(max = 120) String valor,
            boolean esPrincipal,
            @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (dado de baja).")
            String estado) {
    }

    public record ContactoResponse(
            Long id, Long personaId, String tipo, String valor, boolean esPrincipal, String estado) {

        public static ContactoResponse from(PersonaContacto c) {
            return new ContactoResponse(c.getId(), c.getPersonaId(), c.getTipo(), c.getValor(),
                    c.isEsPrincipal(), c.getEstado());
        }
    }

    // ---------- documentos ----------

    public record DocumentoRequest(
            @NotBlank
            @Pattern(regexp = "CI|PASAPORTE|CROQUIS|RESPALDO|OTRO",
                    message = "El tipo de documento debe ser CI, PASAPORTE, CROQUIS, RESPALDO u OTRO.")
            String tipo,
            @Size(max = 50) String numero,
            @Size(max = 200) String descripcion,
            @Size(max = 300) String rutaArchivo,
            LocalDate fechaEmision,
            LocalDate fechaVencimiento,
            @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (dado de baja).")
            String estado) {
    }

    public record DocumentoResponse(
            Long id, Long personaId, String tipo, String numero, String descripcion,
            String rutaArchivo, LocalDate fechaEmision, LocalDate fechaVencimiento, String estado,
            boolean vencido) {

        public static DocumentoResponse from(PersonaDocumento d) {
            return new DocumentoResponse(d.getId(), d.getPersonaId(), d.getTipo(), d.getNumero(),
                    d.getDescripcion(), d.getRutaArchivo(), d.getFechaEmision(),
                    d.getFechaVencimiento(), d.getEstado(), d.estaVencido(LocalDate.now()));
        }
    }
}
