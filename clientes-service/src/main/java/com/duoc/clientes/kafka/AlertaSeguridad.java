package com.duoc.clientes.kafka;

import java.math.BigDecimal;
import java.time.Instant;

/** Evento publicado por pagos-service ante monto alto u operacion rechazada. */
public record AlertaSeguridad(
        String eventoId, String tipoAlerta, Long clienteId, Long cuentaId,
        BigDecimal monto, String detalle, Instant fecha) {
}
