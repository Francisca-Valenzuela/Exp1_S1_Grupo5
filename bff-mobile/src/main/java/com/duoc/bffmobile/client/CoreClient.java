package com.duoc.bffmobile.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.duoc.bffmobile.dto.CuentaCoreDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/** Historial batch (opcional): si el core no responde, el movil recibe el saldo sin movimientos recientes. */
@Component
public class CoreClient {

    private final RestClient restClient;

    public CoreClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
                      @Value("${services.core:banco-xyz-core}") String serviceId) {
        this.restClient = builder.baseUrl("http://" + serviceId).build();
    }

    @Retry(name = "coreService")
    @CircuitBreaker(name = "coreService", fallbackMethod = "obtenerCuentaFallback")
    public CuentaCoreDTO obtenerCuenta(Long cuentaId) {
        try {
            return restClient.get().uri("/internal/cuentas/{id}", cuentaId).retrieve().body(CuentaCoreDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private CuentaCoreDTO obtenerCuentaFallback(Long cuentaId, Throwable ex) {
        return null;
    }
}
