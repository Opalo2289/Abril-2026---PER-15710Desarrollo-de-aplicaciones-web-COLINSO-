package com.alquiler.operaciones.web.dto;

import com.alquiler.operaciones.domain.EstadoSolicitud;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Solicitud de alquiler persistida en el microservicio operaciones")
public record SolicitudResponse(
        @Schema(description = "Identificador interno de la solicitud") Long id,
        @Schema(description = "Referencia al vehículo en el otro microservicio") Long vehiculoId,
        @Schema(description = "Inicio del periodo") LocalDate fechaInicio,
        @Schema(description = "Fin del periodo") LocalDate fechaFin,
        @Schema(description = "PENDIENTE, CONFIRMADA o CANCELADA") EstadoSolicitud estado
) {
}
