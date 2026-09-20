package com.duoc.bancoxyzbatch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas_interes")
public class CuentaInteresEntity {

    @Id
    private Long cuentaId;

    private String nombre;
    private Double saldoInicial;
    private Double saldoFinal;
    private Integer edad;
    private String tipo;

    // getters y setters
    public Long getCuentaId() { 
        return cuentaId; 
    }
    
    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    
    public Double getSaldoInicial() { 
        return saldoInicial; 
    }
    
    public void setSaldoInicial(Double saldoInicial) { 
        this.saldoInicial = saldoInicial; 
    }
    
    public Double getSaldoFinal() { 
        return saldoFinal; 
    }
    
    public void setSaldoFinal(Double saldoFinal) { 
        this.saldoFinal = saldoFinal; 
    }
    
    public Integer getEdad() { 
        return edad; 
    }
    
    public void setEdad(Integer edad) { 
        this.edad = edad; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }
}