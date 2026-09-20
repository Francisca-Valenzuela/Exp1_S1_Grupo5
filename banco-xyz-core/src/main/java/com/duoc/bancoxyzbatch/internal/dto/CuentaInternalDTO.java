package com.duoc.bancoxyzbatch.internal.dto;

import java.util.List;

/**
 * Vista "completa" de una cuenta que expone banco-xyz-core hacia la red
 * interna. Cada BFF (web/mobile/atm) recorta este payload segun lo que su
 * canal realmente necesita mostrar (patron Backend for Frontend).
 */
public class CuentaInternalDTO {

    private Long cuentaId;
    private String nombre;
    private Integer edad;
    private String tipo;
    private Double saldoInicial;
    private Double saldoFinal;
    private List<MovimientoInternalDTO> movimientos;

    public CuentaInternalDTO() {
    }

    public CuentaInternalDTO(Long cuentaId, String nombre, Integer edad, String tipo,
                              Double saldoInicial, Double saldoFinal,
                              List<MovimientoInternalDTO> movimientos) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.edad = edad;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.saldoFinal = saldoFinal;
        this.movimientos = movimientos;
    }

    public Long getCuentaId() { return cuentaId; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public String getTipo() { return tipo; }
    public Double getSaldoInicial() { return saldoInicial; }
    public Double getSaldoFinal() { return saldoFinal; }
    public List<MovimientoInternalDTO> getMovimientos() { return movimientos; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSaldoInicial(Double saldoInicial) { this.saldoInicial = saldoInicial; }
    public void setSaldoFinal(Double saldoFinal) { this.saldoFinal = saldoFinal; }
    public void setMovimientos(List<MovimientoInternalDTO> movimientos) { this.movimientos = movimientos; }
}
