package com.duoc.bffweb.dto;

import java.util.List;

/** Canal Web: respuesta COMPLETA (cuenta + perfil del cliente + historial) para interfaces complejas. */
public class CuentaWebDTO {

    private Long cuentaId;
    private Long clienteId;
    private String nombre;
    private Integer edad;
    private String email;
    private String perfil;
    private String nivelRiesgo;
    private String tipo;
    private String estado;
    private Double saldoActual;
    private Double saldoInicialLegacy;
    private List<MovimientoWebDTO> historialMovimientos;
    /** true si algun servicio opcional (clientes o core) no respondio y la respuesta es parcial. */
    private boolean datosParciales;

    public Long getCuentaId() { return cuentaId; }
    public Long getClienteId() { return clienteId; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public String getEmail() { return email; }
    public String getPerfil() { return perfil; }
    public String getNivelRiesgo() { return nivelRiesgo; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public Double getSaldoActual() { return saldoActual; }
    public Double getSaldoInicialLegacy() { return saldoInicialLegacy; }
    public List<MovimientoWebDTO> getHistorialMovimientos() { return historialMovimientos; }
    public boolean isDatosParciales() { return datosParciales; }

    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setEmail(String email) { this.email = email; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setSaldoActual(Double saldoActual) { this.saldoActual = saldoActual; }
    public void setSaldoInicialLegacy(Double saldoInicialLegacy) { this.saldoInicialLegacy = saldoInicialLegacy; }
    public void setHistorialMovimientos(List<MovimientoWebDTO> historialMovimientos) { this.historialMovimientos = historialMovimientos; }
    public void setDatosParciales(boolean datosParciales) { this.datosParciales = datosParciales; }
}
