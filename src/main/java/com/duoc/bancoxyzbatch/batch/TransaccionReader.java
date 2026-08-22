package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.duoc.bancoxyzbatch.model.TransaccionCsv;

@Configuration
public class TransaccionReader {

    @Bean
    public SynchronizedItemStreamReader<TransaccionCsv> transaccionItemReader() {
        FlatFileItemReader<TransaccionCsv> delegate = new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionItemReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionCsv.class)
                .build();

        return new SynchronizedItemStreamReaderBuilder<TransaccionCsv>()
                .delegate(delegate)
                .build();
    }
}