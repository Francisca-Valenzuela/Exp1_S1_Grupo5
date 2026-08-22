package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.BatchJobListener;
import com.duoc.bancoxyzbatch.batch.BatchSkipListener;
import com.duoc.bancoxyzbatch.batch.BatchStepListener;
import com.duoc.bancoxyzbatch.batch.CustomSkipPolicy;
import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentaAnualJobConfig {

    @Bean
    public Job cuentaAnualJob(JobRepository jobRepository, Step cuentaAnualStep, BatchJobListener batchJobListener) {
        return new JobBuilder("cuentaAnualJob", jobRepository)
                .start(cuentaAnualStep)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    public Step cuentaAnualStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemStreamReader<CuentaAnualCsv> cuentaAnualItemReader,
                                 ItemProcessor<CuentaAnualCsv, CuentaAnualEntity> cuentaAnualProcessor,
                                 ItemWriter<CuentaAnualEntity> cuentaAnualItemWriter,
                                 TaskExecutor batchTaskExecutor,
                                 CustomSkipPolicy customSkipPolicy,
                                 BatchSkipListener batchSkipListener,
                                 BatchStepListener batchStepListener) {

        return new StepBuilder("cuentaAnualStep", jobRepository)
                .<CuentaAnualCsv, CuentaAnualEntity>chunk(5, transactionManager)
                .reader(cuentaAnualItemReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualItemWriter)
                .taskExecutor(batchTaskExecutor)
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