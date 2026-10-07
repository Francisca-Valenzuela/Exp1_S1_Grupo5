package com.duoc.cuentas.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import com.duoc.cuentas.kafka.Topics;

@Configuration
public class KafkaConfig {

    /**
     * Manejo de errores de los consumidores: reintentos con backoff exponencial y, agotados
     * los intentos, el mensaje se desvia al topico "<topico>.DLT" (Dead Letter Topic) en vez
     * de perderse o bloquear la particion.
     */
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

    // 3 particiones por topico: hasta 3 consumidores por grupo en paralelo (escalado horizontal)
    @Bean public NewTopic cuentasMigradas()    { return topico(Topics.CUENTAS_MIGRADAS); }
    @Bean public NewTopic pagosSolicitados()   { return topico(Topics.PAGOS_SOLICITADOS); }
    @Bean public NewTopic pagosResultado()     { return topico(Topics.PAGOS_RESULTADO); }
    @Bean public NewTopic retirosSolicitados() { return topico(Topics.RETIROS_SOLICITADOS); }
    @Bean public NewTopic retirosResultado()   { return topico(Topics.RETIROS_RESULTADO); }

    private NewTopic topico(String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}
