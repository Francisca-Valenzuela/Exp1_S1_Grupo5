package com.duoc.bffmobile.client;

/** Espejo minimo de GET /api/cuentas/{id}: el movil solo necesita saldo y estado (ahorro de ancho de banda). */
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
