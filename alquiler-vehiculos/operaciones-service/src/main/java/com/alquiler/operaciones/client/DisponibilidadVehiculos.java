package com.alquiler.operaciones.client;

import java.time.LocalDate;

/**
 * Consulta de disponibilidad del vehículo para un rango de fechas.
 * <p>Lote 4: la implementación por defecto es un stub (no llama a {@code vehiculos-service}).
 * Lote 5 sustituirá el bean por integración Feign real.</p>
 */
public interface DisponibilidadVehiculos {

    boolean estaDisponibleParaAlquiler(Long vehiculoId, LocalDate fechaInicio, LocalDate fechaFin);
}
