package com.duoc.bffatm.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.duoc.bffatm.kafka.Topics;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic retirosSolicitados() {
        return TopicBuilder.name(Topics.RETIROS_SOLICITADOS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic retirosResultado() {
        return TopicBuilder.name(Topics.RETIROS_RESULTADO).partitions(3).replicas(1).build();
    }
}
