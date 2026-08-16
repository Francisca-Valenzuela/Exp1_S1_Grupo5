package com.duoc.bancoxyzbatch.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas_anuales")
public class CuentaAnualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;

    // getters y setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public Long getCuentaId() { 
        return cuentaId; 
    }
    
    public void setCuentaId(Long cuentaId) { 
        this.cuentaId = cuentaId;
    }
    
    public LocalDate getFecha() { 
        return fecha; 
    }
    
    public void setFecha(LocalDate fecha) { 
        this.fecha = fecha; 
    }
    
    public String getTransaccion() { 
        return transaccion; 
    }
    
    public void setTransaccion(String transaccion) { 
        this.transaccion = transaccion; 
    }
    
    public Double getMonto() { 
        return monto; 
    }
    
    public void setMonto(Double monto) { 
        this.monto = monto; 
    }
    
    public String getDescripcion() { 
        return descripcion; 
    }
    
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }
}