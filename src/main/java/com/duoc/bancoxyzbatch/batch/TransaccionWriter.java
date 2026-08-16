package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duoc.bancoxyzbatch.entity.TransaccionEntity;
import com.duoc.bancoxyzbatch.repository.TransaccionRepository;

@Configuration
public class TransaccionWriter {

    @Bean
    public RepositoryItemWriter<TransaccionEntity> transaccionItemWriter(TransaccionRepository repository) {
        return new RepositoryItemWriterBuilder<TransaccionEntity>()
                .repository(repository)
                .methodName("save")
                .build();
    }
}