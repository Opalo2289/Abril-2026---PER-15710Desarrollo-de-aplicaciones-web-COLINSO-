package com.alquiler.operaciones.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Datos para registrar una solicitud de alquiler (vehículo referenciado solo por id)")
public record SolicitudRequest(
        @Schema(description = "Identificador del vehículo en el catálogo (microservicio vehiculos-service)", example = "1")
        @NotNull(message = "vehiculoId es obligatorio") Long vehiculoId,
        @Schema(description = "Inicio del periodo de alquiler", example = "2030-07-01")
        @NotNull(message = "fechaInicio es obligatoria") LocalDate fechaInicio,
        @Schema(description = "Fin del periodo de alquiler", example = "2030-07-05")
        @NotNull(message = "fechaFin es obligatoria") LocalDate fechaFin
) {
}
