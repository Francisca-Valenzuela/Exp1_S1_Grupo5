package com.duoc.bancoxyzbatch.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    /** 3 particiones: permite hasta 3 consumidores en paralelo por grupo (escalabilidad horizontal). */
    @Bean
    public NewTopic cuentasMigradasTopic() {
        return TopicBuilder.name(Topics.CUENTAS_MIGRADAS).partitions(3).replicas(1).build();
    }
}
