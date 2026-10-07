package com.duoc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Punto de entrada unico (puerto 8080). Enruta por path hacia los servicios registrados en Eureka
 * (lb://nombre) y reparte la carga entre sus replicas (Spring Cloud LoadBalancer, round-robin).
 * Las rutas viven en config-server (config-repo/api-gateway.yml).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
