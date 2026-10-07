package com.duoc.cuentas.kafka;

import java.math.BigDecimal;

/** Resultado de aplicar una operacion (topicos pagos.resultado y retiros.resultado). estado: APLICADO | RECHAZADO. */
public record OperacionResultado(
        String solicitudId,
        String tipo,
        String estado,
        String motivo,
        Long cuentaOrigenId,
        Long cuentaDestinoId,
        Long clienteId,
        BigDecimal monto,
        BigDecimal saldoOrigen) {
}
