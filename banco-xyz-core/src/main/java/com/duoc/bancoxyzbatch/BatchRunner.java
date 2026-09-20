package com.duoc.bancoxyzbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BatchRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchRunner.class);

    private final JobLauncher jobLauncher;
    private final Job transaccionJob;
    private final Job cuentaInteresJob;
    private final Job cuentaAnualJob;

    public BatchRunner(JobLauncher jobLauncher,
                        Job transaccionJob,
                        Job cuentaInteresJob,
                        Job cuentaAnualJob) {
        this.jobLauncher = jobLauncher;
        this.transaccionJob = transaccionJob;
        this.cuentaInteresJob = cuentaInteresJob;
        this.cuentaAnualJob = cuentaAnualJob;
    }

    @Override
    public void run(String... args) throws Exception {
        long timestamp = System.currentTimeMillis();

        log.info("Ejecutando Job 1: Reporte de transacciones diarias");
        jobLauncher.run(transaccionJob,
                new JobParametersBuilder().addLong("startAt", timestamp).toJobParameters());

        log.info("Ejecutando Job 2: Calculo de intereses mensuales");
        jobLauncher.run(cuentaInteresJob,
                new JobParametersBuilder().addLong("startAt", timestamp).toJobParameters());

        log.info("Ejecutando Job 3: Generacion de estados de cuenta anuales");
        jobLauncher.run(cuentaAnualJob,
                new JobParametersBuilder().addLong("startAt", timestamp).toJobParameters());

        log.info("Los 3 Jobs finalizaron su ejecucion");
    }
}