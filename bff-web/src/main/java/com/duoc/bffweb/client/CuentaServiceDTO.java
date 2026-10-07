package com.duoc.bffweb.client;

import java.time.Instant;

/** Espejo de GET /api/cuentas/{id} de cuentas-service. */
public class CuentaServiceDTO {
    private Long cuentaId;
    private Long clienteId;
    private String tipo;
    private Double saldo;
    private String estado;
    private Instant fechaApertura;

    public Long getCuentaId() { return cuentaId; }
    public Long getClienteId() { return clienteId; }
    public String getTipo() { return tipo; }
    public Double getSaldo() { return saldo; }
    public String getEstado() { return estado; }
    public Instant getFechaApertura() { return fechaApertura; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaApertura(Instant fechaApertura) { this.fechaApertura = fechaApertura; }
}
