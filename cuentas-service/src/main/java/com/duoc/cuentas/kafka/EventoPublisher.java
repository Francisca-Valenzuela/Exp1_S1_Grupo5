package com.duoc.cuentas.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publicacion sincrona (espera el ack del broker): si falla, la excepcion dispara el reintento del consumidor. */
@Component
public class EventoPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventoJson json;

    public EventoPublisher(KafkaTemplate<String, String> kafkaTemplate, EventoJson json) {
        this.kafkaTemplate = kafkaTemplate;
        this.json = json;
    }

    public void publicar(String topico, String clave, Object evento) {
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
