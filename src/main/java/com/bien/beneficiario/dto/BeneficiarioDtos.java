package com.bien.beneficiario.dto;

import com.bien.beneficiario.entity.Beneficiario;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class BeneficiarioDtos {

    private BeneficiarioDtos() {}

    public record BeneficiarioRequest(
            Long personaId,
            @NotBlank @Size(max = 200) String nombreCompleto,
            @NotBlank @Size(max = 30) String parentesco,
            @NotNull
            @DecimalMin(value = "0.01", inclusive = true, message = "El porcentaje debe ser mayor que cero.")
            @DecimalMax(value = "100", message = "El porcentaje no puede pasar de 100.")
            BigDecimal porcentaje,
            @Size(max = 20) String documentoIdentidad,
            @PastOrPresent LocalDate fechaNacimiento,
            @Pattern(regexp = "V|S", message = "El estado debe ser V (vigente) o S (dado de baja).")
            String estado) {
    }

    public record BeneficiarioResponse(
            Long id, Long socioId, Long personaId, String nombreCompleto, String parentesco,
            BigDecimal porcentaje, String documentoIdentidad, LocalDate fechaNacimiento,
            String estado) {

        public static BeneficiarioResponse from(Beneficiario b) {
            return new BeneficiarioResponse(b.getId(), b.getSocioId(), b.getPersonaId(),
                    b.getNombreCompleto(), b.getParentesco(), b.getPorcentaje(),
                    b.getDocumentoIdentidad(), b.getFechaNacimiento(), b.getEstado());
        }
    }
}
