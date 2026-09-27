package com.duoc.bffatm.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.duoc.bffatm.client.CoreClient;
import com.duoc.bffatm.client.CuentaCoreDTO;
import com.duoc.bffatm.dto.CuentaAtmDTO;
import com.duoc.bffatm.dto.RetiroAceptadoDTO;
import com.duoc.bffatm.dto.RetiroEstadoDTO;
import com.duoc.bffatm.exception.CoreNoDisponibleException;
import com.duoc.bffatm.store.RetiroEstadoStore;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class AtmBffService {

    private final CoreClient coreClient;
    private final JmsTemplate jmsTemplate;
    private final RetiroEstadoStore estadoStore;

    public AtmBffService(CoreClient coreClient, JmsTemplate jmsTemplate, RetiroEstadoStore estadoStore) {
        this.coreClient = coreClient;
        this.jmsTemplate = jmsTemplate;
        this.estadoStore = estadoStore;
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

    // Tolerancia a fallos para la mensajería asíncrona (config: instancia "jmsBroker")
    @Retry(name = "jmsBroker")
    @CircuitBreaker(name = "jmsBroker", fallbackMethod = "retirarFallback")
    public RetiroAceptadoDTO retirar(Long cuentaId, Double monto) {
        String solicitudId = UUID.randomUUID().toString();

        // Evento RetiroSolicitado: el cuerpo es el monto, el resto viaja como propiedades
        jmsTemplate.convertAndSend("cola.retiros.pendientes", monto, message -> {
            message.setLongProperty("cuentaId", cuentaId);
            message.setStringProperty("solicitudId", solicitudId);
            return message;
        });

        estadoStore.guardar(new RetiroEstadoDTO(solicitudId, cuentaId, monto, "PENDIENTE", null));

        return new RetiroAceptadoDTO(solicitudId, cuentaId, monto, "PENDIENTE");
    }

    // Fallback: el broker está caído o el circuito está abierto
    public RetiroAceptadoDTO retirarFallback(Long cuentaId, Double monto, Throwable ex) {
        throw new CoreNoDisponibleException(
                "El sistema de transacciones está temporalmente fuera de servicio. Tu retiro no pudo ser encolado.");
    }

    public RetiroEstadoDTO consultarEstado(String solicitudId) {
        RetiroEstadoDTO estado = estadoStore.buscar(solicitudId);
        if (estado == null) {
            throw new NoSuchElementException("Solicitud " + solicitudId + " no encontrada");
        }
        return estado;
    }
}