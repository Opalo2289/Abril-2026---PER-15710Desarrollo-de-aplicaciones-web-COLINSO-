package com.alquiler.vehiculos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "operaciones-service")
public interface OperacionesSolicitudesClient {

    @DeleteMapping("/api/operaciones/solicitudes/vehiculo/{vehiculoId}")
    void eliminarPorVehiculo(@PathVariable("vehiculoId") Long vehiculoId);
}
