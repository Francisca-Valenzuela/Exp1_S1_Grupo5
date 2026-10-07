package com.duoc.pagos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/** Pre-validacion sincrona de cuentas contra cuentas-service (Eureka + LoadBalancer + Resilience4j). */
@Component
public class CuentasClient {

    private static final Logger log = LoggerFactory.getLogger(CuentasClient.class);

    private final RestClient restClient;

    public CuentasClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
                         @Value("${cuentas.service-id:cuentas-service}") String serviceId) {
        this.restClient = builder.baseUrl("http://" + serviceId).build();
    }

    /** @return true si existe, false si no existe, null si no se pudo consultar (fallback). */
    @Retry(name = "cuentasService")
    @CircuitBreaker(name = "cuentasService", fallbackMethod = "existeFallback")
    public Boolean existe(Long cuentaId) {
        try {
            restClient.get().uri("/api/cuentas/{id}", cuentaId).retrieve().toBodilessEntity();
            return Boolean.TRUE;
        } catch (HttpClientErrorException.NotFound e) {
            return Boolean.FALSE;
        }
    }

    // Alternativa ante fallo: no se bloquea el pago; la validacion definitiva la hace cuentas-service
    // de forma asincrona (rechazara la operacion si la cuenta no existe).
    @SuppressWarnings("unused")
    private Boolean existeFallback(Long cuentaId, Throwable causa) {
        log.warn("cuentas-service no disponible al validar cuenta {}: {}. Se acepta y se valida de forma asincrona.",
                cuentaId, causa.toString());
        return null;
    }
}
