package com.duoc.bancoxyz.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de Service Discovery (Eureka) del ecosistema Banco XYZ.
 * Aquí se registran banco-xyz-core, bff-web, bff-mobile y bff-atm.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
