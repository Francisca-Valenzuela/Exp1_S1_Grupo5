package com.duoc.bancoxyzbatch.internal.dto;

public class RetiroInternalDTO {

    private Double monto;

    public RetiroInternalDTO() {
    }

    public RetiroInternalDTO(Double monto) {
        this.monto = monto;
    }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
