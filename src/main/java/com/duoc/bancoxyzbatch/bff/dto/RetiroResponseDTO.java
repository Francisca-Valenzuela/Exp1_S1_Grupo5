package com.duoc.bancoxyzbatch.bff.dto;

public class RetiroResponseDTO {

    private Long cuentaId;
    private Double montoRetirado;
    private Double saldoDisponible;

    public RetiroResponseDTO(Long cuentaId, Double montoRetirado, Double saldoDisponible) {
        this.cuentaId = cuentaId;
        this.montoRetirado = montoRetirado;
        this.saldoDisponible = saldoDisponible;
    }

    public Long getCuentaId() { 
        return cuentaId; 
    }
    
    public Double getMontoRetirado() { 
        return montoRetirado; 
    }
    
    public Double getSaldoDisponible() { 
        return saldoDisponible; 
    }
}