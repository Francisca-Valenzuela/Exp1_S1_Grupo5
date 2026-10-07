package com.duoc.clientes.kafka;

import java.math.BigDecimal;
import java.time.Instant;

/** Evento publicado por pagos-service cuando un movimiento se aplico correctamente. */
public record TransaccionCompletada(
        String eventoId, String solicitudId, String tipo, Long cuentaOrigenId, Long cuentaDestinoId,
        Long clienteId, BigDecimal monto, String canal, Instant fecha) {
}
