package com.alquiler.operaciones.client;

import com.alquiler.operaciones.client.dto.VehiculoCatalogoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia {@code vehiculos-service}.
 * <p>Lote 6: Feign resuelve el nombre {@code vehiculos-service} vía Eureka + Spring Cloud LoadBalancer.
 * Sin atributo {@code url}: la instancia se descubre en el registro de Eureka en tiempo de ejecución.</p>
 */
@FeignClient(name = "vehiculos-service")
public interface VehiculoCatalogoClient {

    @GetMapping("/api/vehiculos/{id}")
    VehiculoCatalogoResponse obtener(@PathVariable("id") Long id);
}
