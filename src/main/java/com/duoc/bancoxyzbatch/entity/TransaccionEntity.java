package com.duoc.bancoxyzbatch.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transacciones_procesadas")
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private Double monto;
    private String tipo;
    private String anomalia; // ej: "monto negativo", "monto cero", null si está OK

    // getters y setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public LocalDate getFecha() { 
        return fecha; 
    }
    
    public void setFecha(LocalDate fecha) { 
        this.fecha = fecha; 
    }
    
    public Double getMonto() { 
        return monto; 
    }
    
    public void setMonto(Double monto) { 
        this.monto = monto; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }
    
    public String getAnomalia() { 
        return anomalia; 
    }
    
    public void setAnomalia(String anomalia) { 
        this.anomalia = anomalia; 
    }
}