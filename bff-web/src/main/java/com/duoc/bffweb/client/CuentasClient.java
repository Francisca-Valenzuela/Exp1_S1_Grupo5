package com.duoc.bffweb.client;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.duoc.bffweb.exception.CoreNoDisponibleException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/** Dato ESENCIAL del canal: si cuentas-service cae, el BFF responde 503 controlado. */
@Component
public class CuentasClient {

    private final RestClient restClient;

    public CuentasClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
                         @Value("${services.cuentas:cuentas-service}") String serviceId) {
        this.restClient = builder.baseUrl("http://" + serviceId).build();
    }

    @Retry(name = "cuentasService")
    @CircuitBreaker(name = "cuentasService", fallbackMethod = "obtenerFallback")
    public CuentaServiceDTO obtener(Long cuentaId) {
        try {
            return restClient.get().uri("/api/cuentas/{id}", cuentaId).retrieve().body(CuentaServiceDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("Cuenta " + cuentaId + " no encontrada");
        }
    }

    @SuppressWarnings("unused")
    private CuentaServiceDTO obtenerFallback(Long cuentaId, Throwable ex) {
        if (ex instanceof NoSuchElementException noEncontrada) {
            throw noEncontrada;
        }
        throw new CoreNoDisponibleException(
                "El servicio de cuentas no esta disponible en este momento. Intenta nuevamente en unos segundos.");
    }
}
