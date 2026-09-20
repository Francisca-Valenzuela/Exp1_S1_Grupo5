package com.duoc.bffmobile.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.duoc.bffmobile.client.CoreClient;
import com.duoc.bffmobile.dto.CuentaCoreDTO;
import com.duoc.bffmobile.dto.CuentaMobileDTO;
import com.duoc.bffmobile.dto.MovimientoCoreDTO;
import com.duoc.bffmobile.dto.MovimientoMobileDTO;
import com.duoc.bffmobile.exception.CoreNoDisponibleException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class MobileBffService {

    // Cantidad de movimientos recientes que se envian al movil, para no
    // sobrecargar la app con el historial completo del ano.
    private static final int MAX_MOVIMIENTOS_RECIENTES = 5;

    private final CoreClient coreClient;

    public MobileBffService(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    @Retry(name = "coreService")
    @CircuitBreaker(name = "coreService", fallbackMethod = "obtenerCuentaFallback")
    public CuentaMobileDTO obtenerCuenta(Long cuentaId) {
        CuentaCoreDTO cuenta = coreClient.obtenerCuenta(cuentaId);

        List<MovimientoMobileDTO> recientes = cuenta.getMovimientos() == null
                ? List.of()
                : cuenta.getMovimientos().stream()
                        .sorted(Comparator.comparing(MovimientoCoreDTO::getFecha).reversed())
                        .limit(MAX_MOVIMIENTOS_RECIENTES)
                        .map(m -> new MovimientoMobileDTO(m.getFecha(), m.getMonto(), m.getDescripcion()))
                        .collect(Collectors.toList());

        return new CuentaMobileDTO(cuenta.getCuentaId(), cuenta.getNombre(), cuenta.getSaldoFinal(), recientes);
    }

    @SuppressWarnings("unused")
    private CuentaMobileDTO obtenerCuentaFallback(Long cuentaId, Throwable ex) {
        throw new CoreNoDisponibleException(
                "El servicio de cuentas no esta disponible en este momento. Intenta nuevamente en unos segundos.");
    }
}
