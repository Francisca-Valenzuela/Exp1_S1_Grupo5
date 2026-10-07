package com.duoc.bffweb.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/** Dato OPCIONAL (enriquecimiento): si clientes-service cae, el fallback devuelve null y la respuesta sale parcial. */
@Component
public class ClientesClient {

    private final RestClient restClient;

    public ClientesClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
                          @Value("${services.clientes:clientes-service}") String serviceId) {
        this.restClient = builder.baseUrl("http://" + serviceId).build();
    }

    @Retry(name = "clientesService")
    @CircuitBreaker(name = "clientesService", fallbackMethod = "obtenerFallback")
    public ClienteServiceDTO obtener(Long clienteId) {
        try {
            return restClient.get().uri("/api/clientes/{id}", clienteId).retrieve().body(ClienteServiceDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private ClienteServiceDTO obtenerFallback(Long clienteId, Throwable ex) {
        return null;
    }
}
