package com.duoc.bancoxyzbatch.bff.dto;

import java.util.List;

/**
 * Vista liviana de una cuenta para el frontend Móvil: solo lo esencial
 * (nombre, saldo actual y los últimos movimientos), sin datos de auditoría
 * ni el historial completo del año, para reducir consumo de ancho de banda.
 */
public class CuentaMobileDTO {

    private Long cuentaId;
    private String nombre;
    private Double saldoActual;
    private List<MovimientoMobileDTO> ultimosMovimientos;

    public CuentaMobileDTO(Long cuentaId, String nombre, Double saldoActual,
                            List<MovimientoMobileDTO> ultimosMovimientos) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldoActual = saldoActual;
        this.ultimosMovimientos = ultimosMovimientos;
    }

    public Long getCuentaId() { return cuentaId; }
    public String getNombre() { return nombre; }
    public Double getSaldoActual() { return saldoActual; }
    public List<MovimientoMobileDTO> getUltimosMovimientos() { return ultimosMovimientos; }
}