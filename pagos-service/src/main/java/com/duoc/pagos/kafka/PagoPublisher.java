package com.duoc.pagos.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.duoc.pagos.domain.Pago;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class PagoPublisher {

    private static final Logger log = LoggerFactory.getLogger(PagoPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventoJson json;

    public PagoPublisher(KafkaTemplate<String, String> kafkaTemplate, EventoJson json) {
        this.kafkaTemplate = kafkaTemplate;
        this.json = json;
    }

    /**
     * Publica la solicitud en pagos.solicitados protegida por Retry + Circuit Breaker ("kafkaBroker").
     * Fallback: devuelve false (no lanza); el pago queda con enviado=false y OutboxScheduler lo reintenta.
     */
    @Retry(name = "kafkaBroker")
    @CircuitBreaker(name = "kafkaBroker", fallbackMethod = "publicarSolicitudFallback")
    public boolean publicarSolicitud(Pago pago) {
        OperacionSolicitada op = new OperacionSolicitada(pago.getSolicitudId(), pago.getTipo(),
                pago.getCuentaOrigenId(), pago.getCuentaDestinoId(), pago.getMonto(),
                pago.getCanal(), pago.getDescripcion());
        enviar(Topics.PAGOS_SOLICITADOS, pago.getSolicitudId(), op);
        return true;
    }

    @SuppressWarnings("unused")
    private boolean publicarSolicitudFallback(Pago pago, Throwable causa) {
        log.warn("Kafka no disponible para solicitud {}: {}. Queda en outbox.", pago.getSolicitudId(), causa.toString());
        return false;
    }

    /** Publicacion de eventos de salida (transacciones.completadas, alertas.seguridad). */
    public void publicarEvento(String topico, String clave, Object evento) {
        enviar(topico, clave, evento);
    }

    private void enviar(String topico, String clave, Object evento) {
        try {
            kafkaTemplate.send(topico, clave, json.toJson(evento)).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicacion interrumpida en " + topico, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("No se pudo publicar en " + topico + ": " + e.getMessage(), e);
        }
    }
}
