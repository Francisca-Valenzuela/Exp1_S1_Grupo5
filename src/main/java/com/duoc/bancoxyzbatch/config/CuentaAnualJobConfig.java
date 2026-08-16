package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.DatoInvalidoException;
import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentaAnualJobConfig {

    @Bean
    public Job cuentaAnualJob(JobRepository jobRepository, Step cuentaAnualStep) {
        return new JobBuilder("cuentaAnualJob", jobRepository)
                .start(cuentaAnualStep)
                .build();
    }

    @Bean
    public Step cuentaAnualStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemReader<CuentaAnualCsv> cuentaAnualItemReader,
                                 ItemProcessor<CuentaAnualCsv, CuentaAnualEntity> cuentaAnualProcessor,
                                 ItemWriter<CuentaAnualEntity> cuentaAnualItemWriter) {
        return new StepBuilder("cuentaAnualStep", jobRepository)
                .<CuentaAnualCsv, CuentaAnualEntity>chunk(5, transactionManager)
                .reader(cuentaAnualItemReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualItemWriter)
                .faultTolerant()
                .skip(DatoInvalidoException.class)
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .listener(new SkipListener<CuentaAnualCsv, CuentaAnualEntity>() {
                    @Override
                    public void onSkipInProcess(CuentaAnualCsv item, Throwable t) {
                        System.out.println("SKIP: se omitió estado de cuenta anual por error -> " + t.getMessage());
                    }
                })
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .build();
    }
}