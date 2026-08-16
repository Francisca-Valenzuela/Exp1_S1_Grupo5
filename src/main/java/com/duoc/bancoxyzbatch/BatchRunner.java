package com.duoc.bancoxyzbatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BatchRunner implements CommandLineRunner {

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
        // cada ejecución necesita parámetros únicos (timestamp) para que
        // Spring Batch no la trate como una instancia ya completada
        long timestamp = System.currentTimeMillis();

        System.out.println("=== Ejecutando Job 1: Reporte de transacciones diarias ===");
        jobLauncher.run(transaccionJob,
                new JobParametersBuilder().addLong("startAt", timestamp).toJobParameters());

        System.out.println("=== Ejecutando Job 2: Cálculo de intereses mensuales ===");
        jobLauncher.run(cuentaInteresJob,
                new JobParametersBuilder().addLong("startAt", timestamp).toJobParameters());

        System.out.println("=== Ejecutando Job 3: Generación de estados de cuenta anuales ===");
        jobLauncher.run(cuentaAnualJob,
                new JobParametersBuilder().addLong("startAt", timestamp).toJobParameters());

        System.out.println("=== Los 3 Jobs finalizaron su ejecución ===");
    }
}