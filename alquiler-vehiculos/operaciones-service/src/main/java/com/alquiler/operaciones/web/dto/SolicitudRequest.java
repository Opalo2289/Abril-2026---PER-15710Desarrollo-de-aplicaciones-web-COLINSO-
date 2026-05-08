package com.alquiler.operaciones.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudRequest(
        @NotNull(message = "vehiculoId es obligatorio") Long vehiculoId,
        @NotNull(message = "fechaInicio es obligatoria") LocalDate fechaInicio,
        @NotNull(message = "fechaFin es obligatoria") LocalDate fechaFin
) {
}
