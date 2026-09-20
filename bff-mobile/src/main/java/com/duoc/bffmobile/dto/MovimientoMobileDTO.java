package com.duoc.bffmobile.dto;

import java.time.LocalDate;

public class MovimientoMobileDTO {

    private LocalDate fecha;
    private Double monto;
    private String descripcion;

    public MovimientoMobileDTO() {
    }

    public MovimientoMobileDTO(LocalDate fecha, Double monto, String descripcion) {
        this.fecha = fecha;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() { return fecha; }
    public Double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }

    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setMonto(Double monto) { this.monto = monto; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
