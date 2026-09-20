package com.duoc.bffatm.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.duoc.bffatm.dto.RetiroResponseDTO;

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

    public CuentaCoreDTO consultarSaldo(Long cuentaId) {
        return restClient.get()
                .uri("/internal/cuentas/{id}", cuentaId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(CuentaCoreDTO.class);
    }

    public RetiroResponseDTO retirar(Long cuentaId, Double monto) {
        return restClient.post()
                .uri("/internal/cuentas/{id}/retiro", cuentaId)
                .header("X-Internal-Api-Key", internalApiKey)
                .body(new RetiroCoreRequestDTO(monto))
                .retrieve()
                .body(RetiroResponseDTO.class);
    }
}
