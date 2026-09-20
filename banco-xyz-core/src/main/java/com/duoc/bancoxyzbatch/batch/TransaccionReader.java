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

import com.duoc.bancoxyzbatch.model.TransaccionCsv;

@Configuration
public class TransaccionReader {

    /**
     * Step-scoped: cada partición del transaccionWorkerStep recibe su propia instancia,
     * con startLine/linesToRead inyectados desde el ExecutionContext que arma
     * LineRangePartitioner (late binding, ver guia Semana 3).
     */
    @Bean
    @StepScope
    public SynchronizedItemStreamReader<TransaccionCsv> transaccionItemReader(
            @Value("#{stepExecutionContext['startLine'] ?: 0}") Long startLine,
            @Value("#{stepExecutionContext['linesToRead']}") Long linesToRead) {

        FlatFileItemReader<TransaccionCsv> delegate = new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionItemReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                // se salta el header (1) mas las lineas de datos de las particiones anteriores
                .linesToSkip((int) (1 + startLine))
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionCsv.class)
                .build();

        if (linesToRead != null) {
            // limita la lectura a las lineas que le corresponden a esta particion
            delegate.setMaxItemCount(linesToRead.intValue());
        }

        return new SynchronizedItemStreamReaderBuilder<TransaccionCsv>()
                .delegate(delegate)
                .build();
    }
}