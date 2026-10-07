package com.duoc.pagos.kafka;

import java.math.BigDecimal;
import java.time.Instant;

public record TransaccionCompletada(
        String eventoId, String solicitudId, String tipo, Long cuentaOrigenId, Long cuentaDestinoId,
        Long clienteId, BigDecimal monto, String canal, Instant fecha) {
}
