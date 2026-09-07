package com.duoc.bancoxyzbatch.bff.dto;

import java.time.LocalDate;

/**
 * DTO completo para el frontend Web: incluye todos los campos,
 * incluida la anomalía, útil para vistas de auditoría/administración.
 */
public class TransaccionWebDTO {

    private Long id;
    private LocalDate fecha;
    private Double monto;
    private String tipo;
    private String anomalia;

    public TransaccionWebDTO(Long id, LocalDate fecha, Double monto, String tipo, String anomalia) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.anomalia = anomalia;
    }

    public Long getId() { 
        return id; 
    }
    
    public LocalDate getFecha() { 
        return fecha; 
    }
    
    public Double getMonto() { 
        return monto; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    
    public String getAnomalia() { 
        return anomalia; 
    }
}