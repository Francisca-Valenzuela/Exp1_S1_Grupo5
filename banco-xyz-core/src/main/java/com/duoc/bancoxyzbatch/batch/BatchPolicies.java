package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.infrastructure.repeat.CompletionPolicy;
import org.springframework.batch.infrastructure.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.infrastructure.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.infrastructure.repeat.policy.TimeoutTerminationPolicy;

/**
 * Politica de finalizacion de cada chunk: el chunk se confirma cuando se
 * alcanza el tamano configurado O cuando transcurre el tiempo maximo, lo que
 * ocurra primero. Evita transacciones largas y bloqueos cuando el volumen es
 * grande o la base responde lento.
 */
public final class BatchPolicies {

    private BatchPolicies() {
    }

    public static CompletionPolicy finalizacionDeChunk(int tamanoChunk, long timeoutMs) {
        CompositeCompletionPolicy politica = new CompositeCompletionPolicy();
        politica.setPolicies(new CompletionPolicy[] {
                new SimpleCompletionPolicy(tamanoChunk),
                new TimeoutTerminationPolicy(timeoutMs)
        });
        return politica;
    }
}
