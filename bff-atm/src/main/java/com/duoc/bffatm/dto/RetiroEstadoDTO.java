package com.duoc.bffatm.dto;

public class RetiroEstadoDTO {

    private String solicitudId;
    private Long cuentaId;
    private Double monto;
    private String estado;
    private String motivo;

    public RetiroEstadoDTO() {
    }

    public RetiroEstadoDTO(String solicitudId, Long cuentaId, Double monto, String estado, String motivo) {
        this.solicitudId = solicitudId;
        this.cuentaId = cuentaId;
        this.monto = monto;
        this.estado = estado;
        this.motivo = motivo;
    }

    public String getSolicitudId() { return solicitudId; }
    public Long getCuentaId() { return cuentaId; }
    public Double getMonto() { return monto; }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }

    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setMonto(Double monto) { this.monto = monto; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}