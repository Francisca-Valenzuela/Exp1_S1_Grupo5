package com.duoc.bancoxyzbatch.config;

import com.duoc.bancoxyzbatch.batch.DatoInvalidoException;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;
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
public class CuentaInteresJobConfig {

    @Bean
    public Job cuentaInteresJob(JobRepository jobRepository, Step cuentaInteresStep) {
        return new JobBuilder("cuentaInteresJob", jobRepository)
                .start(cuentaInteresStep)
                .build();
    }

    @Bean
    public Step cuentaInteresStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   ItemReader<CuentaInteresCsv> cuentaInteresItemReader,
                                   ItemProcessor<CuentaInteresCsv, CuentaInteresEntity> cuentaInteresProcessor,
                                   ItemWriter<CuentaInteresEntity> cuentaInteresItemWriter) {
        return new StepBuilder("cuentaInteresStep", jobRepository)
                .<CuentaInteresCsv, CuentaInteresEntity>chunk(5, transactionManager)
                .reader(cuentaInteresItemReader)
                .processor(cuentaInteresProcessor)
                .writer(cuentaInteresItemWriter)
                .faultTolerant()
                .skip(DatoInvalidoException.class)
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .listener(new SkipListener<CuentaInteresCsv, CuentaInteresEntity>() {
                    @Override
                    public void onSkipInProcess(CuentaInteresCsv item, Throwable t) {
                        System.out.println("SKIP: se omitió cuenta de interés por error -> " + t.getMessage());
                    }
                })
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .build();
    }
}