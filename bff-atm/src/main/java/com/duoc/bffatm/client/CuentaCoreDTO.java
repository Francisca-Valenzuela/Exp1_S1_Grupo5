package com.duoc.bffatm.client;

/**
 * Espejo minimo del payload de /internal/cuentas/{id}: al cajero solo le
 * interesan cuentaId y saldoFinal (el resto del JSON se ignora al
 * deserializar).
 */
public class CuentaCoreDTO {
    private Long cuentaId;
    private Double saldoFinal;

    public Long getCuentaId() { return cuentaId; }
    public Double getSaldoFinal() { return saldoFinal; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setSaldoFinal(Double saldoFinal) { this.saldoFinal = saldoFinal; }
}
