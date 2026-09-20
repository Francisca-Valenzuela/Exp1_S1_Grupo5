package com.duoc.bancoxyzbatch.batch;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== Inicio job '{}' (id={}) ===",
                jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duracion = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
        log.info("=== Fin job '{}' -> estado: {}, duracion: {} ms ===",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                duracion.toMillis());
    }
}