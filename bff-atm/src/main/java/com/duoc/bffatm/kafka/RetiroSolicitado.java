package com.duoc.bffatm.kafka;

import java.math.BigDecimal;

/** Evento "RetiroSolicitado" (comando para cuentas-service). Mismo contrato que OperacionSolicitada. */
public record RetiroSolicitado(
        String solicitudId, String tipo, Long cuentaOrigenId, Long cuentaDestinoId,
        BigDecimal monto, String canal, String descripcion) {
}
