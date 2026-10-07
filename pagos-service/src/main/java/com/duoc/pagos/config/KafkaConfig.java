package com.duoc.pagos.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import com.duoc.pagos.kafka.Topics;

@Configuration
public class KafkaConfig {

    /** Reintentos con backoff exponencial y, al agotarse, desvio a "<topico>.DLT" (Dead Letter Topic). */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (registro, error) -> new TopicPartition(registro.topic() + ".DLT", -1));
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(4);
        backoff.setInitialInterval(500);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(5000);
        return new DefaultErrorHandler(recoverer, backoff);
    }

    @Bean public NewTopic pagosSolicitados()         { return topico(Topics.PAGOS_SOLICITADOS); }
    @Bean public NewTopic pagosResultado()           { return topico(Topics.PAGOS_RESULTADO); }
    @Bean public NewTopic transaccionesCompletadas() { return topico(Topics.TRANSACCIONES_COMPLETADAS); }
    @Bean public NewTopic alertasSeguridad()         { return topico(Topics.ALERTAS_SEGURIDAD); }

    private NewTopic topico(String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}
