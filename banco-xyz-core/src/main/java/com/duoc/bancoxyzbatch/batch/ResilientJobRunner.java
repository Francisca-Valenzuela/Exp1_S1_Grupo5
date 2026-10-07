package com.duoc.bancoxyzbatch.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Reejecucion automatica ante fallos criticos. Si un job termina en FAILED
 * (p. ej. Kafka o PostgreSQL caidos), se relanza con LOS MISMOS parametros:
 * Spring Batch reconoce la instancia fallida y la REINICIA, saltandose los
 * steps ya completados (y las particiones ya terminadas). Entre intentos
 * espera un backoff creciente.
 */
@Component
public class ResilientJobRunner {

    private static final Logger log = LoggerFactory.getLogger(ResilientJobRunner.class);

    private final JobLauncher jobLauncher;
    private final int maxIntentos;
    private final long esperaBaseMs;

    public ResilientJobRunner(JobLauncher jobLauncher,
                              @Value("${batch.restart.max-attempts:3}") int maxIntentos,
                              @Value("${batch.restart.delay-ms:5000}") long esperaBaseMs) {
        this.jobLauncher = jobLauncher;
        this.maxIntentos = maxIntentos;
        this.esperaBaseMs = esperaBaseMs;
    }

    public JobExecution ejecutar(Job job, JobParameters parametros) throws Exception {
        JobExecution ejecucion = null;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            ejecucion = jobLauncher.run(job, parametros);
            if (ejecucion.getStatus() != BatchStatus.FAILED) {
                return ejecucion;
            }
            log.error("Job '{}' FALLO (intento {}/{}). Se reiniciara desde el step fallido.",
                    job.getName(), intento, maxIntentos);
            if (intento < maxIntentos) {
                Thread.sleep(esperaBaseMs * intento);
            }
        }
        log.error("Job '{}' agoto los {} intentos: requiere atencion manual.", job.getName(), maxIntentos);
        return ejecucion;
    }
}
