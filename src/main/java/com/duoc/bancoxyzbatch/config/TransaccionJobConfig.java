package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.BatchJobListener;
import com.duoc.bancoxyzbatch.batch.BatchSkipListener;
import com.duoc.bancoxyzbatch.batch.BatchStepListener;
import com.duoc.bancoxyzbatch.batch.CustomSkipPolicy;
import com.duoc.bancoxyzbatch.entity.TransaccionEntity;
import com.duoc.bancoxyzbatch.model.TransaccionCsv;
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
public class TransaccionJobConfig {

    @Bean
    public Job transaccionJob(JobRepository jobRepository, Step transaccionStep, BatchJobListener batchJobListener) {
        return new JobBuilder("transaccionJob", jobRepository)
                .start(transaccionStep)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    public Step transaccionStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemStreamReader<TransaccionCsv> transaccionItemReader,
                                 ItemProcessor<TransaccionCsv, TransaccionEntity> transaccionProcessor,
                                 ItemWriter<TransaccionEntity> transaccionItemWriter,
                                 TaskExecutor batchTaskExecutor,
                                 CustomSkipPolicy customSkipPolicy,
                                 BatchSkipListener batchSkipListener,
                                 BatchStepListener batchStepListener) {

        return new StepBuilder("transaccionStep", jobRepository)
                .<TransaccionCsv, TransaccionEntity>chunk(5, transactionManager)
                .reader(transaccionItemReader)
                .processor(transaccionProcessor)
                .writer(transaccionItemWriter)
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