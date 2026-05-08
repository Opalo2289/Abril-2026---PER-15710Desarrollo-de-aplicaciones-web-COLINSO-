package com.alquiler.operaciones.client.dto;

import java.math.BigDecimal;

/**
 * DTO minimal del catálogo de vehículos recibido desde {@code vehiculos-service}.
 * Se usa {@code String} para el estado para evitar acoplamiento al enum de ese módulo.
 */
public record VehiculoCatalogoResponse(
        Long id,
        String marca,
        String modelo,
        String matricula,
        String estado,
        BigDecimal precioPorDia
) {
}
