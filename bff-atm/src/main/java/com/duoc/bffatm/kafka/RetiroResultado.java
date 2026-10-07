package com.duoc.bffatm.kafka;

import java.math.BigDecimal;

/** Evento "RetiroProcesado"/"RetiroRechazado" publicado por cuentas-service (estado APLICADO | RECHAZADO). */
public record RetiroResultado(
        String solicitudId, String tipo, String estado, String motivo, Long cuentaOrigenId,
        Long cuentaDestinoId, Long clienteId, BigDecimal monto, BigDecimal saldoOrigen) {
}
