package com.duoc.bancoxyzbatch.model;

public class CuentaAnualCsv {
    private Long cuentaId;
    private String fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;

    // getters y setters
    public Long getCuentaId() { 
        return cuentaId; 
    }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getFecha() { 
        return fecha; 
    }

    public void setFecha(String fecha) { 
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