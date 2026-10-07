package com.duoc.cuentas.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Llamada sincrona a clientes-service (descubierto por Eureka, balanceado en cliente).
 * Protegida con Resilience4j: Retry + Circuit Breaker. Si clientes-service no responde,
 * el fallback devuelve null y la apertura continua en modo degradado (PENDIENTE_VALIDACION).
 */
@Component
public class ClientesClient {

    private static final Logger log = LoggerFactory.getLogger(ClientesClient.class);

    private final RestClient restClient;

    public ClientesClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
                          @Value("${clientes.service-id:clientes-service}") String serviceId) {
        this.restClient = builder.baseUrl("http://" + serviceId).build();
    }

    /** @return true si existe, false si no existe, null si no se pudo consultar (fallback). */
    @Retry(name = "clientesService")
    @CircuitBreaker(name = "clientesService", fallbackMethod = "existeFallback")
    public Boolean existe(Long clienteId) {
        try {
            restClient.get().uri("/api/clientes/{id}", clienteId).retrieve().toBodilessEntity();
            return Boolean.TRUE;
        } catch (HttpClientErrorException.NotFound e) {
            return Boolean.FALSE;
        }
    }

    @SuppressWarnings("unused")
    private Boolean existeFallback(Long clienteId, Throwable causa) {
        log.warn("clientes-service no disponible (cliente {}): {}. Modo degradado.", clienteId, causa.toString());
        return null;
    }
}
