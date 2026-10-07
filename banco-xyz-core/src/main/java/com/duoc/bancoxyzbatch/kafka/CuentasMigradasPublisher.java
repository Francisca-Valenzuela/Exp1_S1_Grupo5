package com.duoc.bancoxyzbatch.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;

/**
 * Publica en Kafka las cuentas ya migradas (tabla cuentas_interes) para que
 * los microservicios nuevos construyan su propia base de datos. Se ejecuta
 * como ultimo step de cuentaInteresJob: si Kafka no responde el step falla,
 * el job queda FAILED y ResilientJobRunner lo reejecuta (solo este step).
 */
@Component
public class CuentasMigradasPublisher {

    private static final Logger log = LoggerFactory.getLogger(CuentasMigradasPublisher.class);
    private static final int TAMANO_PAGINA = 100;

    private final CuentaInteresRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventoJson json;

    public CuentasMigradasPublisher(CuentaInteresRepository repository,
                                    KafkaTemplate<String, String> kafkaTemplate,
                                    EventoJson json) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.json = json;
    }

    public int publicarTodas() {
        int pagina = 0;
        int total = 0;
        Page<CuentaInteresEntity> actual;
        do {
            actual = repository.findAll(PageRequest.of(pagina++, TAMANO_PAGINA, Sort.by("cuentaId")));
            List<CompletableFuture<?>> envios = new ArrayList<>();
            for (CuentaInteresEntity c : actual.getContent()) {
                CuentaMigradaEvent evento = new CuentaMigradaEvent(
                        UUID.randomUUID().toString(),
                        c.getCuentaId(),
                        c.getCuentaId(),
                        c.getNombre(),
                        c.getEdad(),
                        c.getTipo(),
                        aDecimal(c.getSaldoInicial()),
                        aDecimal(c.getSaldoFinal()),
                        Instant.now());
                // la clave = cuentaId: todos los eventos de una cuenta caen en la misma particion (orden garantizado)
                envios.add(kafkaTemplate.send(Topics.CUENTAS_MIGRADAS,
                        String.valueOf(c.getCuentaId()), json.toJson(evento)));
            }
            esperar(envios);
            total += envios.size();
        } while (actual.hasNext());

        log.info("Eventos '{}' publicados: {}", Topics.CUENTAS_MIGRADAS, total);
        return total;
    }

    private void esperar(List<CompletableFuture<?>> envios) {
        try {
            CompletableFuture.allOf(envios.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicacion a Kafka interrumpida", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("No se pudo publicar en Kafka: " + e.getMessage(), e);
        }
    }

    private BigDecimal aDecimal(Double valor) {
        return valor == null ? BigDecimal.ZERO : BigDecimal.valueOf(valor);
    }
}
