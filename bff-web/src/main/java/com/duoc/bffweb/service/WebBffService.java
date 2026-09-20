package com.duoc.bffweb.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.duoc.bffweb.client.CoreClient;
import com.duoc.bffweb.client.CuentaCoreDTO;
import com.duoc.bffweb.client.MovimientoCoreDTO;
import com.duoc.bffweb.dto.CuentaWebDTO;
import com.duoc.bffweb.dto.MovimientoWebDTO;
import com.duoc.bffweb.dto.TransaccionWebDTO;
import com.duoc.bffweb.exception.CoreNoDisponibleException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Capa de negocio del BFF Web. Cada metodo que llama a banco-xyz-core queda
 * protegido con Resilience4j:
 *  - @Retry: reintenta ante fallas transitorias (ej. timeout puntual).
 *  - @CircuitBreaker: si las fallas se repiten, "abre el circuito" y deja de
 *    insistir contra un servicio caido, respondiendo de inmediato via el
 *    metodo de fallback (patron Fallback / Respuesta alternativa).
 */
@Service
public class WebBffService {

    private final CoreClient coreClient;

    public WebBffService(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    @Retry(name = "coreService")
    @CircuitBreaker(name = "coreService", fallbackMethod = "obtenerCuentaFallback")
    public CuentaWebDTO obtenerCuenta(Long cuentaId) {
        CuentaCoreDTO cuenta = coreClient.obtenerCuenta(cuentaId);

        List<MovimientoWebDTO> historial = cuenta.getMovimientos() == null
                ? List.of()
                : cuenta.getMovimientos().stream().map(this::toMovimientoWebDTO).collect(Collectors.toList());

        return new CuentaWebDTO(
                cuenta.getCuentaId(), cuenta.getNombre(), cuenta.getEdad(), cuenta.getTipo(),
                cuenta.getSaldoInicial(), cuenta.getSaldoFinal(), historial);
    }

    @Retry(name = "coreService")
    @CircuitBreaker(name = "coreService", fallbackMethod = "listarTransaccionesFallback")
    public Page<TransaccionWebDTO> listarTransacciones(Pageable pageable) {
        return coreClient.listarTransacciones(pageable);
    }

    private MovimientoWebDTO toMovimientoWebDTO(MovimientoCoreDTO m) {
        return new MovimientoWebDTO(m.getId(), m.getFecha(), m.getTransaccion(), m.getMonto(), m.getDescripcion());
    }

    // --- Fallbacks: se ejecutan cuando banco-xyz-core no responde, responde
    // con error, o el circuito esta abierto. El BFF nunca deja al canal Web
    // sin respuesta: informa la degradacion explicitamente. ---

    @SuppressWarnings("unused")
    private CuentaWebDTO obtenerCuentaFallback(Long cuentaId, Throwable ex) {
        throw new CoreNoDisponibleException(
                "El servicio de cuentas no esta disponible en este momento. Intenta nuevamente en unos segundos.");
    }

    @SuppressWarnings("unused")
    private Page<TransaccionWebDTO> listarTransaccionesFallback(Pageable pageable, Throwable ex) {
        return new PageImpl<>(List.of());
    }
}
