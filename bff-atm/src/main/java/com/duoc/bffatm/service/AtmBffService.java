package com.duoc.bffatm.service;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.duoc.bffatm.client.CoreClient;
import com.duoc.bffatm.client.CuentaCoreDTO;
import com.duoc.bffatm.dto.CuentaAtmDTO;
import com.duoc.bffatm.dto.RetiroResponseDTO;
import com.duoc.bffatm.exception.CoreNoDisponibleException;
import com.duoc.bffatm.exception.SaldoInsuficienteException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class AtmBffService {

    private final CoreClient coreClient;

    public AtmBffService(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    @Retry(name = "coreService")
    @CircuitBreaker(name = "coreService", fallbackMethod = "consultarSaldoFallback")
    public CuentaAtmDTO consultarSaldo(Long cuentaId) {
        try {
            CuentaCoreDTO cuenta = coreClient.consultarSaldo(cuentaId);
            return new CuentaAtmDTO(cuenta.getCuentaId(), cuenta.getSaldoFinal());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new NoSuchElementException("Cuenta " + cuentaId + " no encontrada");
        }
    }

    // El retiro NO lleva @CircuitBreaker/@Retry: es una operacion que
    // modifica saldo, y reintentarla automaticamente ante una respuesta
    // ambigua (timeout) podria duplicar un descuento. Sí se deja que, si
    // core esta completamente caido, la llamada falle rapido y se informe
    // al cajero, en vez de bloquear la operacion indefinidamente.
    public RetiroResponseDTO retirar(Long cuentaId, Double monto) {
        try {
            return coreClient.retirar(cuentaId, monto);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new NoSuchElementException("Cuenta " + cuentaId + " no encontrada");
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw new SaldoInsuficienteException(
                        "Saldo insuficiente en la cuenta " + cuentaId + " para retirar " + monto);
            }
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero");
            }
            throw new CoreNoDisponibleException("No fue posible procesar el retiro. Intenta nuevamente.");
        }
    }

    @SuppressWarnings("unused")
    private CuentaAtmDTO consultarSaldoFallback(Long cuentaId, Throwable ex) {
        if (ex instanceof NoSuchElementException) {
            throw (NoSuchElementException) ex;
        }
        throw new CoreNoDisponibleException(
                "El servicio de cuentas no esta disponible en este momento. Intenta nuevamente en unos segundos.");
    }
}
