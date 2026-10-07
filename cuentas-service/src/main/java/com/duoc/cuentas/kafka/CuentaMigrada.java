package com.duoc.cuentas.kafka;

import java.math.BigDecimal;
import java.time.Instant;

/** Evento publicado por banco-xyz-core al terminar la migracion batch de una cuenta. */
public record CuentaMigrada(
        String eventoId,
        Long cuentaId,
        Long clienteId,
        String nombre,
        Integer edad,
        String tipo,
        BigDecimal saldoInicial,
        BigDecimal saldoFinal,
        Instant fecha) {
}
