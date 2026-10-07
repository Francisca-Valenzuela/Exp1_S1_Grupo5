package com.duoc.bffweb.client;

/** Espejo de GET /api/clientes/{id} de clientes-service. */
public class ClienteServiceDTO {
    private Long clienteId;
    private String nombre;
    private Integer edad;
    private String email;
    private String perfil;
    private String estado;
    private String nivelRiesgo;

    public Long getClienteId() { return clienteId; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public String getEmail() { return email; }
    public String getPerfil() { return perfil; }
    public String getEstado() { return estado; }
    public String getNivelRiesgo() { return nivelRiesgo; }

    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setEmail(String email) { this.email = email; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
}
