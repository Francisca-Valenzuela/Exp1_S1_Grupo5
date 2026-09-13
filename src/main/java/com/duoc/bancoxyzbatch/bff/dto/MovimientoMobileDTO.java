package com.duoc.bancoxyzbatch.bff.dto;

import java.time.LocalDate;

/**
 * Movimiento reducido a lo esencial para pantalla móvil:
 * sin id interno ni campos de auditoría.
 */
public class MovimientoMobileDTO {

    private LocalDate fecha;
    private Double monto;
    private String descripcion;

    public MovimientoMobileDTO(LocalDate fecha, Double monto, String descripcion) {
        this.fecha = fecha;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() { return fecha; }
    public Double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
}