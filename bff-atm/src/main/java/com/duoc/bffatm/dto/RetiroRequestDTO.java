package com.duoc.bffatm.dto;

public class RetiroRequestDTO {

    private Double monto;

    public RetiroRequestDTO() {
    }

    public RetiroRequestDTO(Double monto) {
        this.monto = monto;
    }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
