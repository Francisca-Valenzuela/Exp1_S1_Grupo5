package com.duoc.cuentas.dto;

import java.math.BigDecimal;

public record AperturaCuentaRequest(Long clienteId, String tipo, BigDecimal saldoInicial) {
}
