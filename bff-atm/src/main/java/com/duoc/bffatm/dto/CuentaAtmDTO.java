package com.duoc.bffatm.dto;

public class CuentaAtmDTO {

    private Long cuentaId;
    private Double saldoDisponible;

    public CuentaAtmDTO() {
    }

    public CuentaAtmDTO(Long cuentaId, Double saldoDisponible) {
        this.cuentaId = cuentaId;
        this.saldoDisponible = saldoDisponible;
    }

    public Long getCuentaId() { return cuentaId; }
    public Double getSaldoDisponible() { return saldoDisponible; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setSaldoDisponible(Double saldoDisponible) { this.saldoDisponible = saldoDisponible; }
}
