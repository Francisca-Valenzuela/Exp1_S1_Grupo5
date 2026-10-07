package com.duoc.bffatm.service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.duoc.bffatm.client.CuentaServiceDTO;
import com.duoc.bffatm.client.CuentasClient;
import com.duoc.bffatm.dto.CuentaAtmDTO;
import com.duoc.bffatm.dto.RetiroAceptadoDTO;
import com.duoc.bffatm.dto.RetiroEstadoDTO;
import com.duoc.bffatm.exception.CoreNoDisponibleException;
import com.duoc.bffatm.kafka.EventoJson;
import com.duoc.bffatm.kafka.RetiroSolicitado;
import com.duoc.bffatm.kafka.Topics;
import com.duoc.bffatm.store.RetiroEstadoStore;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class AtmBffService {

    private final CuentasClient cuentasClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventoJson json;
    private final RetiroEstadoStore estadoStore;
    private final double montoMaximo;
    private final long multiplo;

    public AtmBffService(CuentasClient cuentasClient, KafkaTemplate<String, String> kafkaTemplate,
                         EventoJson json, RetiroEstadoStore estadoStore,
                         @Value("${atm.retiro.monto-maximo:500000}") double montoMaximo,
                         @Value("${atm.retiro.multiplo:1000}") long multiplo) {
        this.cuentasClient = cuentasClient;
        this.kafkaTemplate = kafkaTemplate;
        this.json = json;
        this.estadoStore = estadoStore;
        this.montoMaximo = montoMaximo;
        this.multiplo = multiplo;
    }

    /** Respuesta minima para el cajero: solo cuentaId y saldo. */
    public CuentaAtmDTO consultarSaldo(Long cuentaId) {
        CuentaServiceDTO cuenta = cuentasClient.obtener(cuentaId);
        return new CuentaAtmDTO(cuenta.getCuentaId(), cuenta.getSaldo());
    }

    /**
     * Publica el evento RetiroSolicitado en Kafka (topico retiros.solicitados) protegido con Retry +
     * Circuit Breaker ("kafkaBroker"). Responde de inmediato; el resultado llega por retiros.resultado.
     */
    @Retry(name = "kafkaBroker")
    @CircuitBreaker(name = "kafkaBroker", fallbackMethod = "retirarFallback")
    public RetiroAceptadoDTO retirar(Long cuentaId, Double monto) {
        validarMonto(monto);
        String solicitudId = UUID.randomUUID().toString();

        RetiroSolicitado evento = new RetiroSolicitado(solicitudId, "RETIRO", cuentaId, null,
                BigDecimal.valueOf(monto), "ATM", "Retiro en cajero automatico");
        try {
            // la clave = cuentaId: los retiros de una misma cuenta se procesan en orden
            kafkaTemplate.send(Topics.RETIROS_SOLICITADOS, String.valueOf(cuentaId), json.toJson(evento))
                    .get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envio interrumpido", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("No se pudo publicar el retiro: " + e.getMessage(), e);
        }

        estadoStore.guardar(new RetiroEstadoDTO(solicitudId, cuentaId, monto, "PENDIENTE", null));
        return new RetiroAceptadoDTO(solicitudId, cuentaId, monto, "PENDIENTE");
    }

    @SuppressWarnings("unused")
    private RetiroAceptadoDTO retirarFallback(Long cuentaId, Double monto, Throwable ex) {
        // un monto invalido sigue siendo un 400, no una caida del sistema
        if (ex instanceof IllegalArgumentException invalido) {
            throw invalido;
        }
        throw new CoreNoDisponibleException(
                "El sistema de transacciones esta temporalmente fuera de servicio. Tu retiro no pudo ser encolado.");
    }

    public RetiroEstadoDTO consultarEstado(String solicitudId) {
        RetiroEstadoDTO estado = estadoStore.buscar(solicitudId);
        if (estado == null) {
            throw new NoSuchElementException("Solicitud " + solicitudId + " no encontrada");
        }
        return estado;
    }

    private void validarMonto(Double monto) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero");
        }
        if (monto > montoMaximo) {
            throw new IllegalArgumentException("El monto maximo por retiro es " + (long) montoMaximo);
        }
        if (monto % multiplo != 0) {
            throw new IllegalArgumentException("El monto debe ser multiplo de " + multiplo);
        }
    }
}
