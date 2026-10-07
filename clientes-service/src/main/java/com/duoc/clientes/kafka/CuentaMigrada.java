package com.duoc.clientes.kafka;

import java.math.BigDecimal;
import java.time.Instant;

public record CuentaMigrada(
        String eventoId, Long cuentaId, Long clienteId, String nombre, Integer edad,
        String tipo, BigDecimal saldoInicial, BigDecimal saldoFinal, Instant fecha) {
}
