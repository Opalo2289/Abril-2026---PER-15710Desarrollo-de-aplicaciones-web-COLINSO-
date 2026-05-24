package com.alquiler.vehiculos.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * No llama a operaciones-service: los tests de vehiculos con H2 no requieren el otro microservicio.
 */
@Component
@Profile("test")
public class StubSolicitudesOperaciones implements SolicitudesOperaciones {

    @Override
    public void eliminarPorVehiculoId(Long vehiculoId) {
        // no-op
    }
}
