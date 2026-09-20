package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.BatchJobListener;
import com.duoc.bancoxyzbatch.batch.BatchSkipListener;
import com.duoc.bancoxyzbatch.batch.BatchStepListener;
import com.duoc.bancoxyzbatch.batch.CustomSkipPolicy;
import com.duoc.bancoxyzbatch.batch.partition.LineRangePartitioner;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;
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
public class CuentaInteresJobConfig {

    @Bean
    public Job cuentaInteresJob(JobRepository jobRepository, Step cuentaInteresPartitionStep, BatchJobListener batchJobListener) {
        return new JobBuilder("cuentaInteresJob", jobRepository)
                .start(cuentaInteresPartitionStep)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    public Step cuentaInteresPartitionStep(JobRepository jobRepository,
                                            Step cuentaInteresWorkerStep,
                                            Partitioner cuentaInteresPartitioner,
                                            TaskExecutor batchTaskExecutor,
                                            @Value("${batch.partition.grid-size:3}") int gridSize) {

        return new StepBuilder("cuentaInteresPartitionStep", jobRepository)
                .partitioner("cuentaInteresWorkerStep", cuentaInteresPartitioner)
                .step(cuentaInteresWorkerStep)
                .gridSize(gridSize)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Partitioner cuentaInteresPartitioner() {
        return new LineRangePartitioner(new ClassPathResource("data/intereses.csv"));
    }

    @Bean
    public Step cuentaInteresWorkerStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         ItemStreamReader<CuentaInteresCsv> cuentaInteresItemReader,
                                         ItemProcessor<CuentaInteresCsv, CuentaInteresEntity> cuentaInteresProcessor,
                                         ItemWriter<CuentaInteresEntity> cuentaInteresItemWriter,
                                         CustomSkipPolicy customSkipPolicy,
                                         BatchSkipListener batchSkipListener,
                                         BatchStepListener batchStepListener) {

        return new StepBuilder("cuentaInteresWorkerStep", jobRepository)
                .<CuentaInteresCsv, CuentaInteresEntity>chunk(5, transactionManager)
                .reader(cuentaInteresItemReader)
                .processor(cuentaInteresProcessor)
                .writer(cuentaInteresItemWriter)
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
