package com.duoc.bancoxyzbatch.batch;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class BatchStepListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Inicio de step '{}' en hilo [{}]",
                stepExecution.getStepName(), Thread.currentThread().getName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Duration duracion = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime());
        log.info("Fin de step '{}' -> leidos: {}, escritos: {}, omitidos: {}, estado: {}, duracion: {} ms",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getExitStatus().getExitCode(),
                duracion.toMillis());
        return stepExecution.getExitStatus();
    }
}