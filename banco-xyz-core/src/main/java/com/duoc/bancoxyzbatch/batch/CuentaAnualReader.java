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

import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;

@Configuration
public class CuentaAnualReader {

    /**
     * Step-scoped: cada particion del cuentaAnualWorkerStep recibe su propia instancia,
     * con startLine/linesToRead inyectados desde el ExecutionContext (LineRangePartitioner).
     */
    @Bean
    @StepScope
    public SynchronizedItemStreamReader<CuentaAnualCsv> cuentaAnualItemReader(
            @Value("#{stepExecutionContext['startLine'] ?: 0}") Long startLine,
            @Value("#{stepExecutionContext['linesToRead']}") Long linesToRead) {

        FlatFileItemReader<CuentaAnualCsv> delegate = new FlatFileItemReaderBuilder<CuentaAnualCsv>()
                .name("cuentaAnualItemReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip((int) (1 + startLine))
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .targetType(CuentaAnualCsv.class)
                .build();

        if (linesToRead != null) {
            delegate.setMaxItemCount(linesToRead.intValue());
        }

        return new SynchronizedItemStreamReaderBuilder<CuentaAnualCsv>()
                .delegate(delegate)
                .build();
    }
}