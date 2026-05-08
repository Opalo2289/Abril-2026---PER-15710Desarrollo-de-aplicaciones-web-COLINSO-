package com.alquiler.operaciones.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Stub de disponibilidad activo únicamente con el perfil {@code test}.
 * Siempre devuelve {@code true} para que los tests con H2 no dependan de
 * {@code vehiculos-service}. En los demás perfiles (desarrollo, producción)
 * el bean activo es {@link FeignDisponibilidadVehiculos}.
 */
@Component
@Profile("test")
public class StubDisponibilidadVehiculos implements DisponibilidadVehiculos {

    @Override
    public boolean estaDisponibleParaAlquiler(Long vehiculoId, LocalDate fechaInicio, LocalDate fechaFin) {
        return true;
    }
}
