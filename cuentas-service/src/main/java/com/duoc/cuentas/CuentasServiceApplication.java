package com.duoc.cuentas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CuentasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuentasServiceApplication.class, args);
    }
}
