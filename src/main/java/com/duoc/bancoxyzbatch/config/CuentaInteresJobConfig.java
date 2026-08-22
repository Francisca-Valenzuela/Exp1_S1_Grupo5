package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.BatchJobListener;
import com.duoc.bancoxyzbatch.batch.BatchSkipListener;
import com.duoc.bancoxyzbatch.batch.BatchStepListener;
import com.duoc.bancoxyzbatch.batch.CustomSkipPolicy;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;
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
public class CuentaInteresJobConfig {

    @Bean
    public Job cuentaInteresJob(JobRepository jobRepository, Step cuentaInteresStep, BatchJobListener batchJobListener) {
        return new JobBuilder("cuentaInteresJob", jobRepository)
                .start(cuentaInteresStep)
                .listener(batchJobListener)
                .build();
    }

    @Bean
    public Step cuentaInteresStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   ItemStreamReader<CuentaInteresCsv> cuentaInteresItemReader,
                                   ItemProcessor<CuentaInteresCsv, CuentaInteresEntity> cuentaInteresProcessor,
                                   ItemWriter<CuentaInteresEntity> cuentaInteresItemWriter,
                                   TaskExecutor batchTaskExecutor,
                                   CustomSkipPolicy customSkipPolicy,
                                   BatchSkipListener batchSkipListener,
                                   BatchStepListener batchStepListener) {

        return new StepBuilder("cuentaInteresStep", jobRepository)
                .<CuentaInteresCsv, CuentaInteresEntity>chunk(5, transactionManager)
                .reader(cuentaInteresItemReader)
                .processor(cuentaInteresProcessor)
                .writer(cuentaInteresItemWriter)
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