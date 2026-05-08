package com.alquiler.operaciones.web.dto;

import com.alquiler.operaciones.domain.EstadoSolicitud;

import java.time.LocalDate;

public record SolicitudResponse(
        Long id,
        Long vehiculoId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoSolicitud estado
) {
}
