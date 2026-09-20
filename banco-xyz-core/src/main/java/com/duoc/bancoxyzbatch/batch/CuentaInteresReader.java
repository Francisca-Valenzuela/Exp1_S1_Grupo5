package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;

@Configuration
public class CuentaInteresReader {

    /**
     * Step-scoped: cada particion del cuentaInteresWorkerStep recibe su propia instancia,
     * con startLine/linesToRead inyectados desde el ExecutionContext (LineRangePartitioner).
     */
    @Bean
    @StepScope
    public SynchronizedItemStreamReader<CuentaInteresCsv> cuentaInteresItemReader(
            @Value("#{stepExecutionContext['startLine'] ?: 0}") Long startLine,
            @Value("#{stepExecutionContext['linesToRead']}") Long linesToRead) {

        FlatFileItemReader<CuentaInteresCsv> delegate = new FlatFileItemReaderBuilder<CuentaInteresCsv>()
                .name("cuentaInteresItemReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip((int) (1 + startLine))
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(CuentaInteresCsv.class)
                .build();

        if (linesToRead != null) {
            delegate.setMaxItemCount(linesToRead.intValue());
        }

        return new SynchronizedItemStreamReaderBuilder<CuentaInteresCsv>()
                .delegate(delegate)
                .build();
    }
}