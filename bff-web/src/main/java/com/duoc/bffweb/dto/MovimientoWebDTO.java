package com.duoc.bffweb.dto;

import java.time.LocalDate;

public class MovimientoWebDTO {

    private Long id;
    private LocalDate fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;

    public MovimientoWebDTO() {
    }

    public MovimientoWebDTO(Long id, LocalDate fecha, String transaccion, Double monto, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public String getTransaccion() { return transaccion; }
    public Double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }

    public void setId(Long id) { this.id = id; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setTransaccion(String transaccion) { this.transaccion = transaccion; }
    public void setMonto(Double monto) { this.monto = monto; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
