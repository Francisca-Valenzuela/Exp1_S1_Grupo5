package com.duoc.bffatm.dto;

public class RetiroAceptadoDTO {

    private String solicitudId;
    private Long cuentaId;
    private Double monto;
    private String estado;

    public RetiroAceptadoDTO() {
    }

    public RetiroAceptadoDTO(String solicitudId, Long cuentaId, Double monto, String estado) {
        this.solicitudId = solicitudId;
        this.cuentaId = cuentaId;
        this.monto = monto;
        this.estado = estado;
    }

    public String getSolicitudId() { 
        return solicitudId; 
    }

    public Long getCuentaId() { 
        return cuentaId; 
    }
    
    public Double getMonto() { 
        return monto; 
    }
    
    public String getEstado() { 
        return estado; 
    }

    
    public void setSolicitudId(String solicitudId) { 
        this.solicitudId = solicitudId; 
    }
    
    public void setCuentaId(Long cuentaId) { 
        this.cuentaId = cuentaId; 
    }
    
    public void setMonto(Double monto) { 
        this.monto = monto; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}