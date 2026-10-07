package com.duoc.bancoxyzbatch.kafka;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento "CuentaMigrada": resultado del job de intereses que el core entrega
 * a cuentas-service y clientes-service. El legacy no tiene tabla de clientes,
 * por lo que clienteId == cuentaId (relacion 1 a 1 en la carga inicial).
 */
public record CuentaMigradaEvent(
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
