package com.duoc.clientes.dto;

import java.time.Instant;

import com.duoc.clientes.domain.Cliente;

public record ClienteResponse(
        Long clienteId,
        String nombre,
        Integer edad,
        String email,
        String telefono,
        String perfil,
        String estado,
        int alertasSeguridad,
        String nivelRiesgo,
        int operacionesCompletadas,
        Instant ultimaActividad) {

    public static ClienteResponse desde(Cliente c) {
        // el nivel de riesgo se deriva de las alertas de seguridad recibidas por Kafka
        String riesgo = c.getAlertasSeguridad() >= 5 ? "ALTO" : c.getAlertasSeguridad() >= 2 ? "MEDIO" : "BAJO";
        return new ClienteResponse(c.getId(), c.getNombre(), c.getEdad(), c.getEmail(), c.getTelefono(),
                c.getPerfil(), c.getEstado(), c.getAlertasSeguridad(), riesgo,
                c.getOperacionesCompletadas(), c.getUltimaActividad());
    }
}
