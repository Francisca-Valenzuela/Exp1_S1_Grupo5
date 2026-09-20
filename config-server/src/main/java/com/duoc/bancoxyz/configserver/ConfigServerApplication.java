package com.duoc.bancoxyz.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Servidor de configuración centralizada del ecosistema Banco XYZ.
 * Sirve, desde config-repo/, la configuración de banco-xyz-core, bff-web,
 * bff-mobile y bff-atm, de modo que cualquier cambio de parámetros
 * (puertos, url de Eureka, parámetros de Resilience4j, etc.) se realiza en
 * un solo lugar y todos los microservicios lo reciben automáticamente.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
