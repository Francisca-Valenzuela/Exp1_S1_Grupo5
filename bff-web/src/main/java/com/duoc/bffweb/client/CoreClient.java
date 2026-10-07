package com.duoc.bffweb.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.duoc.bffweb.dto.TransaccionWebDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Cliente de banco-xyz-core: datos generados por los procesos batch (movimientos anuales y reporte de
 * transacciones). Es informacion historica: si el core no responde, se degrada (vacio) en vez de fallar.
 */
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
            return null;   // cuenta creada por API: no tiene historial legacy
        }
    }

    @SuppressWarnings("unused")
    private CuentaCoreDTO obtenerCuentaFallback(Long cuentaId, Throwable ex) {
        return null;
    }

    @Retry(name = "coreService")
    @CircuitBreaker(name = "coreService", fallbackMethod = "listarTransaccionesFallback")
    public Page<TransaccionWebDTO> listarTransacciones(Pageable pageable) {
        CorePageResponse<TransaccionWebDTO> respuesta = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/transacciones")
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<CorePageResponse<TransaccionWebDTO>>() {});

        if (respuesta == null || respuesta.getContent() == null) {
            return new PageImpl<>(List.of());
        }
        return new PageImpl<>(respuesta.getContent(), pageable, respuesta.getTotalElements());
    }

    @SuppressWarnings("unused")
    private Page<TransaccionWebDTO> listarTransaccionesFallback(Pageable pageable, Throwable ex) {
        return new PageImpl<>(List.of());
    }
}
