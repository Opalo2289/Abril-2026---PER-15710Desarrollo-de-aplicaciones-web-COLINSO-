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
                        .description("""
                                API REST de solicitudes de alquiler (UNIR PER-15710). \
                                La disponibilidad del catálogo se consulta vía **Feign** al microservicio \
                                `vehiculos-service`, resuelto por **Eureka** y Spring Cloud **LoadBalancer**. \
                                Cada MS tiene su propia base de datos; no hay acceso JPA cruzado.""")
                        .version("1.0.0"));
    }
}
