package com.duoc.bancoxyzbatch.bff.dto;

/**
 * Vista mínima para el cajero automático: solo lo indispensable para
 * una operación de consulta de saldo. Sin nombre, sin edad, sin
 * historial — el cajero no debe exponer más datos de los necesarios.
 */
public class CuentaAtmDTO {

    private Long cuentaId;
    private Double saldoDisponible;

    public CuentaAtmDTO(Long cuentaId, Double saldoDisponible) {
        this.cuentaId = cuentaId;
        this.saldoDisponible = saldoDisponible;
    }

    public Long getCuentaId() { return cuentaId; }
    public Double getSaldoDisponible() { return saldoDisponible; }
}