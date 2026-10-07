package com.duoc.bancoxyzbatch.batch;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;

@Configuration
public class CuentaInteresWriter {

    /**
     * Writer idempotente y seguro en paralelo:
     *  1) elimina duplicados dentro del chunk (gana la ultima fila de cada cuenta);
     *  2) las ordena por cuentaId: todas las transacciones toman los bloqueos de fila en el mismo orden,
     *     asi los 3 workers en paralelo no se interbloquean (deadlock);
     *  3) hace un upsert por cuenta (INSERT ... ON CONFLICT DO UPDATE).
     */
    @Bean
    public ItemWriter<CuentaInteresEntity> cuentaInteresItemWriter(CuentaInteresRepository repository) {
        return chunk -> {
            Map<Long, CuentaInteresEntity> porCuenta = new TreeMap<>();
            for (CuentaInteresEntity item : chunk.getItems()) {
                porCuenta.put(item.getCuentaId(), item);
            }
            porCuenta.values().forEach(repository::upsertar);
        };
    }
}