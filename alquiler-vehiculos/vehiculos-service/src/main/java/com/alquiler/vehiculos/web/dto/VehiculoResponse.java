package com.alquiler.vehiculos.web.dto;

import com.alquiler.vehiculos.domain.EstadoVehiculo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Vehículo expuesto por la API")
public record VehiculoResponse(
        @Schema(description = "Identificador interno") Long id,
        String marca,
        String modelo,
        String matricula,
        EstadoVehiculo estado,
        BigDecimal precioPorDia
) {
}
