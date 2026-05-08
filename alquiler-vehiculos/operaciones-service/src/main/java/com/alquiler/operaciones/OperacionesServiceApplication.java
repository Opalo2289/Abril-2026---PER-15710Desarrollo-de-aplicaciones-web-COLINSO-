package com.alquiler.operaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OperacionesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperacionesServiceApplication.class, args);
    }
}
