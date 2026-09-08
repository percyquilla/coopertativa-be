package com.bien.parametros.dto;

import com.bien.parametros.entity.Institucion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InstitucionResponse(
        Long id,
        String codigo,
        String razonSocial,
        String nombreComercial,
        String nit,
        String matricula,
        String codigoRegulador,
        String direccion,
        String telefono,
        String correo,
        Long monedaBaseId,
        String representanteLegal,
        LocalDate fechaFundacion,
        String estado,
        LocalDateTime fechaHora) {

    public static InstitucionResponse from(Institucion i) {
        return new InstitucionResponse(
                i.getId(),
                i.getCodigo(),
                i.getRazonSocial(),
                i.getNombreComercial(),
                i.getNit(),
                i.getMatricula(),
                i.getCodigoRegulador(),
                i.getDireccion(),
                i.getTelefono(),
                i.getCorreo(),
                i.getMonedaBaseId(),
                i.getRepresentanteLegal(),
                i.getFechaFundacion(),
                i.getEstado(),
                i.getFechaHora());
    }
}
