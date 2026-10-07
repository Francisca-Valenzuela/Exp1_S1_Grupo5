package com.duoc.cuentas.kafka;

import java.math.BigDecimal;

/**
 * Comando de movimiento de dinero (topicos pagos.solicitados y retiros.solicitados).
 * tipo: PAGO | TRANSFERENCIA | DEPOSITO | RETIRO.
 */
public record OperacionSolicitada(
        String solicitudId,
        String tipo,
        Long cuentaOrigenId,
        Long cuentaDestinoId,
        BigDecimal monto,
        String canal,
        String descripcion) {
}
