package com.alquiler.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de descubrimiento Eureka.
 *
 * Al arrancar expone el panel de registro en http://localhost:8761
 * Los microservicios vehículos-service y operaciones-service (lotes 3 y 6)
 * se registrarán aquí al añadir la dependencia eureka-client.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
