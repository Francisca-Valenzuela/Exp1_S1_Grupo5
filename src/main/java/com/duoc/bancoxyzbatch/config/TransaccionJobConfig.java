package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.DatoInvalidoException;
import com.duoc.bancoxyzbatch.entity.TransaccionEntity;
import com.duoc.bancoxyzbatch.model.TransaccionCsv;
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
public class TransaccionJobConfig {

    @Bean
    public Job transaccionJob(JobRepository jobRepository, Step transaccionStep) {
        return new JobBuilder("transaccionJob", jobRepository)
                .start(transaccionStep)
                .build();
    }

    @Bean
    public Step transaccionStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ItemReader<TransaccionCsv> transaccionItemReader,
                                ItemProcessor<TransaccionCsv, TransaccionEntity> transaccionProcessor,
                                ItemWriter<TransaccionEntity> transaccionItemWriter) {
        return new StepBuilder("transaccionStep", jobRepository)
                .<TransaccionCsv, TransaccionEntity>chunk(5, transactionManager)
                .reader(transaccionItemReader)
                .processor(transaccionProcessor)
                .writer(transaccionItemWriter)
                .faultTolerant()
                .skip(DatoInvalidoException.class)
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .listener(new SkipListener<TransaccionCsv, TransaccionEntity>() {
                    @Override
                    public void onSkipInProcess(TransaccionCsv item, Throwable t) {
                        System.out.println("SKIP: se omitió transacción por error -> " + t.getMessage());
                    }
                })
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .build();
    }
}