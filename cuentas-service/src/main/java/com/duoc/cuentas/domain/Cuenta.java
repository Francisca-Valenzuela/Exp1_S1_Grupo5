package com.duoc.cuentas.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    // Las cuentas migradas conservan su id legacy (101..150) y se insertan por SQL nativo;
    // las abiertas por API usan esta secuencia, que arranca en 100000 para no colisionar.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cuentas_seq")
    @SequenceGenerator(name = "cuentas_seq", sequenceName = "cuentas_seq", initialValue = 100000, allocationSize = 1)
    private Long id;

    private Long clienteId;
    private String tipo;

    @Column(precision = 19, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    private EstadoCuenta estado;

    private Instant fechaApertura;
    private String origen; // BATCH (migrada) o API

    // Bloqueo optimista: protege contra actualizaciones concurrentes de la misma fila
    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta estado) { this.estado = estado; }
    public Instant getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Instant fechaApertura) { this.fechaApertura = fechaApertura; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
