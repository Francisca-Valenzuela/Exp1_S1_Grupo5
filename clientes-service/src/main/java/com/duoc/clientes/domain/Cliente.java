package com.duoc.clientes.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "clientes")
public class Cliente {

    // Clientes migrados: conservan id legacy (insert nativo). Clientes por API: secuencia desde 100000.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientes_seq")
    @SequenceGenerator(name = "clientes_seq", sequenceName = "clientes_seq", initialValue = 100000, allocationSize = 1)
    private Long id;

    private String nombre;
    private Integer edad;
    private String email;
    private String telefono;
    private String perfil;   // BASICO | PREMIUM
    private String estado;   // ACTIVO | INACTIVO
    private int alertasSeguridad;
    private int operacionesCompletadas;
    private Instant ultimaActividad;
    private Instant fechaRegistro;
    private String origen;   // BATCH | API

    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getAlertasSeguridad() { return alertasSeguridad; }
    public void setAlertasSeguridad(int alertasSeguridad) { this.alertasSeguridad = alertasSeguridad; }
    public int getOperacionesCompletadas() { return operacionesCompletadas; }
    public void setOperacionesCompletadas(int operacionesCompletadas) { this.operacionesCompletadas = operacionesCompletadas; }
    public Instant getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(Instant ultimaActividad) { this.ultimaActividad = ultimaActividad; }
    public Instant getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
