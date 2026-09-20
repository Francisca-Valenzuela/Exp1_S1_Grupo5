package com.duoc.bancoxyzbatch.internal.dto;

import java.time.LocalDate;

public class TransaccionInternalDTO {

    private Long id;
    private LocalDate fecha;
    private Double monto;
    private String tipo;
    private String anomalia;

    public TransaccionInternalDTO() {
    }

    public TransaccionInternalDTO(Long id, LocalDate fecha, Double monto, String tipo, String anomalia) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.anomalia = anomalia;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public Double getMonto() { return monto; }
    public String getTipo() { return tipo; }
    public String getAnomalia() { return anomalia; }

    public void setId(Long id) { this.id = id; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setMonto(Double monto) { this.monto = monto; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setAnomalia(String anomalia) { this.anomalia = anomalia; }
}
