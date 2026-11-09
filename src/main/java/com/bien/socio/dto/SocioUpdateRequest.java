package com.bien.socio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** El número de socio no es modificable (Obs. #4); si el cliente lo envía, se ignora. */
public record SocioUpdateRequest(
        @NotNull Long sucursalId,
        @NotNull @PastOrPresent LocalDate fechaIngreso,
        @Size(max = 50) String usuario) {
}
