package com.duoc.pagos.kafka;

import java.math.BigDecimal;

public record OperacionSolicitada(
        String solicitudId, String tipo, Long cuentaOrigenId, Long cuentaDestinoId,
        BigDecimal monto, String canal, String descripcion) {
}
