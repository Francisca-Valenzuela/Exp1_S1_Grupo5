package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;

@Configuration
public class CuentaInteresWriter {

    @Bean
    public RepositoryItemWriter<CuentaInteresEntity> cuentaInteresItemWriter(CuentaInteresRepository repository) {
        return new RepositoryItemWriterBuilder<CuentaInteresEntity>()
                .repository(repository)
                .methodName("save")
                .build();
    }
}