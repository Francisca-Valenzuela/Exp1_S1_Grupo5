package com.duoc.bffmobile.dto;

import java.util.List;

/** Espejo del payload de /internal/cuentas/{id} en banco-xyz-core. */
public class CuentaCoreDTO {

    private Long cuentaId;
    private String nombre;
    private Integer edad;
    private String tipo;
    private Double saldoInicial;
    private Double saldoFinal;
    private List<MovimientoCoreDTO> movimientos;

    public Long getCuentaId() { return cuentaId; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public String getTipo() { return tipo; }
    public Double getSaldoInicial() { return saldoInicial; }
    public Double getSaldoFinal() { return saldoFinal; }
    public List<MovimientoCoreDTO> getMovimientos() { return movimientos; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSaldoInicial(Double saldoInicial) { this.saldoInicial = saldoInicial; }
    public void setSaldoFinal(Double saldoFinal) { this.saldoFinal = saldoFinal; }
    public void setMovimientos(List<MovimientoCoreDTO> movimientos) { this.movimientos = movimientos; }
}
