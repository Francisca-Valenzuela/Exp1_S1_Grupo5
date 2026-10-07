package com.duoc.bancoxyzbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.batch.ResilientJobRunner;

/**
 * Ejecuta los 3 jobs al arrancar. Se puede desactivar con
 * batch.run-on-startup=false (p. ej. si se escala el core a mas de 1 replica,
 * para que los jobs corran una sola vez).
 */
@Component
@ConditionalOnProperty(name = "batch.run-on-startup", havingValue = "true", matchIfMissing = true)
public class BatchRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchRunner.class);

    private final ResilientJobRunner runner;
    private final Job transaccionJob;
    private final Job cuentaInteresJob;
    private final Job cuentaAnualJob;

    public BatchRunner(ResilientJobRunner runner,
                       Job transaccionJob,
                       Job cuentaInteresJob,
                       Job cuentaAnualJob) {
        this.runner = runner;
        this.transaccionJob = transaccionJob;
        this.cuentaInteresJob = cuentaInteresJob;
        this.cuentaAnualJob = cuentaAnualJob;
    }

    @Override
    public void run(String... args) throws Exception {
        JobParameters parametros = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        log.info("Ejecutando Job 1: Reporte de transacciones diarias");
        JobExecution r1 = runner.ejecutar(transaccionJob, parametros);

        log.info("Ejecutando Job 2: Calculo de intereses mensuales (+ publicacion de cuentas migradas a Kafka)");
        JobExecution r2 = runner.ejecutar(cuentaInteresJob, parametros);

        log.info("Ejecutando Job 3: Generacion de estados de cuenta anuales");
        JobExecution r3 = runner.ejecutar(cuentaAnualJob, parametros);

        log.info("Resumen -> transaccionJob: {}, cuentaInteresJob: {}, cuentaAnualJob: {}",
                r1.getStatus(), r2.getStatus(), r3.getStatus());
    }
}
