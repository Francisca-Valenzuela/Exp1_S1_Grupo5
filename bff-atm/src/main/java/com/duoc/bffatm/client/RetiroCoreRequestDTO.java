package com.duoc.bffatm.client;

public class RetiroCoreRequestDTO {
    private Double monto;

    public RetiroCoreRequestDTO() {
    }

    public RetiroCoreRequestDTO(Double monto) {
        this.monto = monto;
    }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
