package com.duoc.bffmobile.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Bean SIN balanceo: lo usa el cliente interno de Eureka para hablar
    // con localhost:8761. Marcado @Primary para que sea el que reciban
    // los beans que no piden explícitamente el balanceado.
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // Bean CON balanceo: solo para llamadas nuestras a banco-xyz-core
    // vía Eureka + LoadBalancer. Se pide explícitamente con @Qualifier.
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}