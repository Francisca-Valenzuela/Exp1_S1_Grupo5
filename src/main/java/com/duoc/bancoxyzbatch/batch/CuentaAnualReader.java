package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;

@Configuration
public class CuentaAnualReader {

    @Bean
    public SynchronizedItemStreamReader<CuentaAnualCsv> cuentaAnualItemReader() {
        FlatFileItemReader<CuentaAnualCsv> delegate = new FlatFileItemReaderBuilder<CuentaAnualCsv>()
                .name("cuentaAnualItemReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .targetType(CuentaAnualCsv.class)
                .build();

        return new SynchronizedItemStreamReaderBuilder<CuentaAnualCsv>()
                .delegate(delegate)
                .build();
    }
}