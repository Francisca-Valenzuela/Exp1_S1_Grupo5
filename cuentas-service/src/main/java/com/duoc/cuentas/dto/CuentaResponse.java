package com.duoc.cuentas.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.duoc.cuentas.domain.Cuenta;

public record CuentaResponse(
        Long cuentaId,
        Long clienteId,
        String tipo,
        BigDecimal saldo,
        String estado,
        Instant fechaApertura,
        String origen) {

    public static CuentaResponse desde(Cuenta c) {
        return new CuentaResponse(c.getId(), c.getClienteId(), c.getTipo(), c.getSaldo(),
                c.getEstado().name(), c.getFechaApertura(), c.getOrigen());
    }
}
