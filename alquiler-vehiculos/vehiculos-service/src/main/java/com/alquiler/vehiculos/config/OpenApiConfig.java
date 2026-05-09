package com.alquiler.vehiculos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vehiculosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("vehiculos-service")
                        .description("""
                                API REST del catálogo de vehículos (UNIR PER-15710): CRUD, filtros por marca/modelo/estado \
                                y registro en **Eureka**. Expuesto al exterior vía **API Gateway** en `/vehiculos/**`""")
                        .version("1.0.0"));
    }
}
