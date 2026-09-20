package com.duoc.bffweb.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import com.duoc.bffweb.dto.TransaccionWebDTO;

/**
 * Cliente HTTP hacia banco-xyz-core. La URL usa el nombre logico del
 * servicio ("banco-xyz-core"); RestClient.Builder es @LoadBalanced, por lo
 * que Spring Cloud LoadBalancer resuelve la instancia real consultando a
 * Eureka (Service Discovery), sin URLs ni puertos fijos.
 */
@Component
public class CoreClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public CoreClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedBuilder,
                    @Value("${core.service-id}") String coreServiceId,
                    @Value("${internal.api-key}") String internalApiKey) {
        this.restClient = loadBalancedBuilder.baseUrl("http://" + coreServiceId).build();
        this.internalApiKey = internalApiKey;
    }

    public CuentaCoreDTO obtenerCuenta(Long cuentaId) {
        return restClient.get()
                .uri("/internal/cuentas/{id}", cuentaId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(CuentaCoreDTO.class);
    }

    public Page<TransaccionWebDTO> listarTransacciones(Pageable pageable) {
        CorePageResponse<TransaccionWebDTO> respuesta = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/transacciones")
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .build())
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<CorePageResponse<TransaccionWebDTO>>() {});

        if (respuesta == null || respuesta.getContent() == null) {
            return new PageImpl<>(java.util.List.of());
        }
        return new PageImpl<>(respuesta.getContent(), pageable, respuesta.getTotalElements());
    }
}
