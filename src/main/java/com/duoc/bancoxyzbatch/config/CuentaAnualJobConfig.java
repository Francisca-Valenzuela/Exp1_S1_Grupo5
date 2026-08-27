package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.BatchJobListener;
import com.duoc.bancoxyzbatch.batch.BatchSkipListener;
import com.duoc.bancoxyzbatch.batch.BatchStepListener;
import com.duoc.bancoxyzbatch.batch.CustomSkipPolicy;
import com.duoc.bancoxyzbatch.batch.partition.LineRangePartitioner;
import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;
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
public class CuentaAnualJobConfig {

    @Bean
    public Job cuentaAnualJob(JobRepository jobRepository, Step cuentaAnualPartitionStep, BatchJobListener batchJobListener) {
        return new JobBuilder("cuentaAnualJob", jobRepository)
                .start(cuentaAnualPartitionStep)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    public Step cuentaAnualPartitionStep(JobRepository jobRepository,
                                          Step cuentaAnualWorkerStep,
                                          Partitioner cuentaAnualPartitioner,
                                          TaskExecutor batchTaskExecutor,
                                          @Value("${batch.partition.grid-size:3}") int gridSize) {

        return new StepBuilder("cuentaAnualPartitionStep", jobRepository)
                .partitioner("cuentaAnualWorkerStep", cuentaAnualPartitioner)
                .step(cuentaAnualWorkerStep)
                .gridSize(gridSize)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Partitioner cuentaAnualPartitioner() {
        return new LineRangePartitioner(new ClassPathResource("data/cuentas_anuales.csv"));
    }

    @Bean
    public Step cuentaAnualWorkerStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       ItemStreamReader<CuentaAnualCsv> cuentaAnualItemReader,
                                       ItemProcessor<CuentaAnualCsv, CuentaAnualEntity> cuentaAnualProcessor,
                                       ItemWriter<CuentaAnualEntity> cuentaAnualItemWriter,
                                       CustomSkipPolicy customSkipPolicy,
                                       BatchSkipListener batchSkipListener,
                                       BatchStepListener batchStepListener) {

        return new StepBuilder("cuentaAnualWorkerStep", jobRepository)
                .<CuentaAnualCsv, CuentaAnualEntity>chunk(5, transactionManager)
                .reader(cuentaAnualItemReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualItemWriter)
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
