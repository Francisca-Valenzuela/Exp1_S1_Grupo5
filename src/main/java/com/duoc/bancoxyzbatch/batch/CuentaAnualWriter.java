package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.repository.CuentaAnualRepository;

@Configuration
public class CuentaAnualWriter {

    @Bean
    public RepositoryItemWriter<CuentaAnualEntity> cuentaAnualItemWriter(CuentaAnualRepository repository) {
        return new RepositoryItemWriterBuilder<CuentaAnualEntity>()
                .repository(repository)
                .methodName("save")
                .build();
    }
}