package com.duoc.bancoxyzbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchAsyncConfig {

    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 3 hilos fijos, tal como exige la actividad de la semana 2
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        // cola de espera acotada: evita que se disparen más hilos que los definidos
        // cuando llegan más chunks de los que los 3 hilos pueden procesar a la vez
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Batch-Thread-");
        executor.initialize();
        return executor;
    }
}