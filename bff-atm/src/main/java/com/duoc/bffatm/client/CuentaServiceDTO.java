package com.duoc.bffatm.client;

/** Espejo minimo de GET /api/cuentas/{id} de cuentas-service: al cajero solo le importa el saldo y el estado. */
public class CuentaServiceDTO {
    private Long cuentaId;
    private Double saldo;
    private String estado;

    public Long getCuentaId() { return cuentaId; }
    public Double getSaldo() { return saldo; }
    public String getEstado() { return estado; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public void setEstado(String estado) { this.estado = estado; }
}
