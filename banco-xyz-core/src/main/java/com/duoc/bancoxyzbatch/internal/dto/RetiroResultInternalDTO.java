package com.duoc.bancoxyzbatch.internal.dto;

public class RetiroResultInternalDTO {

    private Long cuentaId;
    private Double montoRetirado;
    private Double saldoDisponible;

    public RetiroResultInternalDTO() {
    }

    public RetiroResultInternalDTO(Long cuentaId, Double montoRetirado, Double saldoDisponible) {
        this.cuentaId = cuentaId;
        this.montoRetirado = montoRetirado;
        this.saldoDisponible = saldoDisponible;
    }

    public Long getCuentaId() { return cuentaId; }
    public Double getMontoRetirado() { return montoRetirado; }
    public Double getSaldoDisponible() { return saldoDisponible; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setMontoRetirado(Double montoRetirado) { this.montoRetirado = montoRetirado; }
    public void setSaldoDisponible(Double saldoDisponible) { this.saldoDisponible = saldoDisponible; }
}
