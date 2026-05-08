package com.alquiler.operaciones.client;

import com.alquiler.operaciones.client.dto.VehiculoCatalogoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia {@code vehiculos-service}.
 * <p>Lote 5: URL fija {@code vehiculos.client.url} (por defecto {@code http://localhost:8081}).
 * Lote 6: se elimina el atributo {@code url} y Feign resuelve el nombre
 * {@code vehiculos-service} vía Eureka + LoadBalancer.</p>
 */
@FeignClient(name = "vehiculos-service", url = "${vehiculos.client.url:http://localhost:8081}")
public interface VehiculoCatalogoClient {

    @GetMapping("/api/vehiculos/{id}")
    VehiculoCatalogoResponse obtener(@PathVariable("id") Long id);
}
