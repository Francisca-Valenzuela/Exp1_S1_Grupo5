package com.duoc.bffmobile.dto;

import java.util.List;

public class CuentaMobileDTO {

    private Long cuentaId;
    private String nombre;
    private Double saldoActual;
    private List<MovimientoMobileDTO> ultimosMovimientos;

    public CuentaMobileDTO() {
    }

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

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setSaldoActual(Double saldoActual) { this.saldoActual = saldoActual; }
    public void setUltimosMovimientos(List<MovimientoMobileDTO> ultimosMovimientos) { this.ultimosMovimientos = ultimosMovimientos; }
}
