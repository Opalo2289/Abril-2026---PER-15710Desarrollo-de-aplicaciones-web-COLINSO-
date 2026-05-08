package com.alquiler.operaciones.client;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Simulación de disponibilidad (lote 4). No usa HTTP ni la base de datos de vehículos.
 * Siempre devuelve {@code true}; en el lote 5 se reemplazará por Feign contra el microservicio de vehículos.
 */
@Component
public class StubDisponibilidadVehiculos implements DisponibilidadVehiculos {

    @Override
    public boolean estaDisponibleParaAlquiler(Long vehiculoId, LocalDate fechaInicio, LocalDate fechaFin) {
        return true;
    }
}
