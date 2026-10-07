package com.duoc.pagos.kafka;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertaSeguridad(
        String eventoId, String tipoAlerta, Long clienteId, Long cuentaId,
        BigDecimal monto, String detalle, Instant fecha) {
}
