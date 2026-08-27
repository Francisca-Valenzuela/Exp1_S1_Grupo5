package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.BatchJobListener;
import com.duoc.bancoxyzbatch.batch.BatchSkipListener;
import com.duoc.bancoxyzbatch.batch.BatchStepListener;
import com.duoc.bancoxyzbatch.batch.CustomSkipPolicy;
import com.duoc.bancoxyzbatch.batch.partition.LineRangePartitioner;
import com.duoc.bancoxyzbatch.entity.TransaccionEntity;
import com.duoc.bancoxyzbatch.model.TransaccionCsv;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransaccionJobConfig {

    @Bean
    public Job transaccionJob(JobRepository jobRepository, Step transaccionPartitionStep, BatchJobListener batchJobListener) {
        return new JobBuilder("transaccionJob", jobRepository)
                .start(transaccionPartitionStep)
                .listener(batchJobListener)
                .build();
    }

    /**
     * Semana 3: step "manager" que reparte el transaccionWorkerStep en N particiones
     * (batch.partition.grid-size, probado con 2, 3 y 4) y las ejecuta en paralelo con
     * batchTaskExecutor. Reemplaza el multi-thread a nivel de item que se usaba en la
     * Semana 2 (StepBuilder.taskExecutor()) por escalado a nivel de step.
     */
    @Bean
    public Step transaccionPartitionStep(JobRepository jobRepository,
                                          Step transaccionWorkerStep,
                                          Partitioner transaccionPartitioner,
                                          TaskExecutor batchTaskExecutor,
                                          @Value("${batch.partition.grid-size:3}") int gridSize) {

        return new StepBuilder("transaccionPartitionStep", jobRepository)
                .partitioner("transaccionWorkerStep", transaccionPartitioner)
                .step(transaccionWorkerStep)
                .gridSize(gridSize)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Partitioner transaccionPartitioner() {
        return new LineRangePartitioner(new ClassPathResource("data/transacciones.csv"));
    }

    @Bean
    public Step transaccionWorkerStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       ItemStreamReader<TransaccionCsv> transaccionItemReader,
                                       ItemProcessor<TransaccionCsv, TransaccionEntity> transaccionProcessor,
                                       ItemWriter<TransaccionEntity> transaccionItemWriter,
                                       CustomSkipPolicy customSkipPolicy,
                                       BatchSkipListener batchSkipListener,
                                       BatchStepListener batchStepListener) {

        return new StepBuilder("transaccionWorkerStep", jobRepository)
                .<TransaccionCsv, TransaccionEntity>chunk(5, transactionManager)
                .reader(transaccionItemReader)
                .processor(transaccionProcessor)
                .writer(transaccionItemWriter)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)
                .listener(batchSkipListener)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .backOffPolicy(new ExponentialBackOffPolicy())
                .listener(batchStepListener)
                .build();
    }
}
