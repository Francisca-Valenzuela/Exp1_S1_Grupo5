package com.duoc.pagos.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.duoc.pagos.domain.Pago;

public record PagoResponse(
        String solicitudId, String tipo, Long cuentaOrigenId, Long cuentaDestinoId,
        BigDecimal monto, String estado, String motivo, String canal, Instant creado, Instant actualizado) {

    public static PagoResponse desde(Pago p) {
        return new PagoResponse(p.getSolicitudId(), p.getTipo(), p.getCuentaOrigenId(), p.getCuentaDestinoId(),
                p.getMonto(), p.getEstado(), p.getMotivo(), p.getCanal(), p.getCreado(), p.getActualizado());
    }
}
