package com.duoc.pagos.kafka;

import java.math.BigDecimal;

public record OperacionResultado(
        String solicitudId, String tipo, String estado, String motivo, Long cuentaOrigenId,
        Long cuentaDestinoId, Long clienteId, BigDecimal monto, BigDecimal saldoOrigen) {
}
