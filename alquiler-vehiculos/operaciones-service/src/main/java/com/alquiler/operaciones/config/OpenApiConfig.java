package com.alquiler.operaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI operacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("operaciones-service")
                        .description("API REST de solicitudes de alquiler (UNIR PER-15710). Lote 4: disponibilidad de vehículos simulada (stub); sin Feign.")
                        .version("1.0.0"));
    }
}
