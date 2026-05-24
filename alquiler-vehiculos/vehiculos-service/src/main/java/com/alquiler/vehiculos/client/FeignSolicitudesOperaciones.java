package com.alquiler.vehiculos.client;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("!test")
public class FeignSolicitudesOperaciones implements SolicitudesOperaciones {

    private final OperacionesSolicitudesClient operacionesSolicitudesClient;

    public FeignSolicitudesOperaciones(OperacionesSolicitudesClient operacionesSolicitudesClient) {
        this.operacionesSolicitudesClient = operacionesSolicitudesClient;
    }

    @Override
    public void eliminarPorVehiculoId(Long vehiculoId) {
        operacionesSolicitudesClient.eliminarPorVehiculo(vehiculoId);
    }
}
