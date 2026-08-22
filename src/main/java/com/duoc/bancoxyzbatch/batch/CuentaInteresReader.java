package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;

@Configuration
public class CuentaInteresReader {

    @Bean
    public SynchronizedItemStreamReader<CuentaInteresCsv> cuentaInteresItemReader() {
        FlatFileItemReader<CuentaInteresCsv> delegate = new FlatFileItemReaderBuilder<CuentaInteresCsv>()
                .name("cuentaInteresItemReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(CuentaInteresCsv.class)
                .build();

        return new SynchronizedItemStreamReaderBuilder<CuentaInteresCsv>()
                .delegate(delegate)
                .build();
    }
}