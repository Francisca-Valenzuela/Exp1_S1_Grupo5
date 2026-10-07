package com.duoc.cuentas.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Builder SIN balanceo (lo usa el cliente interno de Eureka). @Primary para beans que no piden el balanceado.
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // Builder CON balanceo (Eureka + Spring Cloud LoadBalancer) y timeouts: una llamada colgada
    // no debe bloquear indefinidamente al hilo (complementa al Circuit Breaker).
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
        factory.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder()
                .requestFactory(factory)
                .requestInterceptor(new TokenRelayInterceptor());
    }
}
