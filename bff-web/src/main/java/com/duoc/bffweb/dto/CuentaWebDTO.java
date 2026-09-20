package com.duoc.bffweb.dto;

import java.util.List;

public class CuentaWebDTO {

    private Long cuentaId;
    private String nombre;
    private Integer edad;
    private String tipo;
    private Double saldoInicial;
    private Double saldoFinal;
    private List<MovimientoWebDTO> historialMovimientos;

    public CuentaWebDTO() {
    }

    public CuentaWebDTO(Long cuentaId, String nombre, Integer edad, String tipo,
                         Double saldoInicial, Double saldoFinal,
                         List<MovimientoWebDTO> historialMovimientos) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.edad = edad;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.saldoFinal = saldoFinal;
        this.historialMovimientos = historialMovimientos;
    }

    public Long getCuentaId() { return cuentaId; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public String getTipo() { return tipo; }
    public Double getSaldoInicial() { return saldoInicial; }
    public Double getSaldoFinal() { return saldoFinal; }
    public List<MovimientoWebDTO> getHistorialMovimientos() { return historialMovimientos; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSaldoInicial(Double saldoInicial) { this.saldoInicial = saldoInicial; }
    public void setSaldoFinal(Double saldoFinal) { this.saldoFinal = saldoFinal; }
    public void setHistorialMovimientos(List<MovimientoWebDTO> historialMovimientos) { this.historialMovimientos = historialMovimientos; }
}
