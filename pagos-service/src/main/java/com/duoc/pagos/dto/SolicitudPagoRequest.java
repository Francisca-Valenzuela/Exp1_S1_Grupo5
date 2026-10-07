package com.duoc.pagos.dto;

import java.math.BigDecimal;

public record SolicitudPagoRequest(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, String descripcion) {
}
