package com.duoc.clientes.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.duoc.clientes.service.ClienteService;

/** Consumidores de clientes-service (grupo "clientes-service"). */
@Component
public class ClientesListeners {

    private static final Logger log = LoggerFactory.getLogger(ClientesListeners.class);

    private final EventoJson json;
    private final ClienteService service;

    public ClientesListeners(EventoJson json, ClienteService service) {
        this.json = json;
        this.service = service;
    }

    @KafkaListener(topics = Topics.CUENTAS_MIGRADAS)
    public void onCuentaMigrada(String payload) {
        CuentaMigrada evento = json.fromJson(payload, CuentaMigrada.class);
        boolean nuevo = service.registrarMigrado(evento);
        log.info("CuentaMigrada {} -> cliente {}", evento.cuentaId(), nuevo ? "creado" : "ya existia");
    }

    /** Actualiza la actividad del cliente con cada transaccion completada. */
    @KafkaListener(topics = Topics.TRANSACCIONES_COMPLETADAS)
    public void onTransaccionCompletada(String payload) {
        TransaccionCompletada evento = json.fromJson(payload, TransaccionCompletada.class);
        if (evento.clienteId() != null) {
            service.registrarOperacion(evento.clienteId());
        }
        log.info("Transaccion completada {} ({}) registrada para cliente {}",
                evento.solicitudId(), evento.tipo(), evento.clienteId());
    }

    /** Alertas de seguridad: aumentan el nivel de riesgo del cliente. */
    @KafkaListener(topics = Topics.ALERTAS_SEGURIDAD)
    public void onAlerta(String payload) {
        AlertaSeguridad alerta = json.fromJson(payload, AlertaSeguridad.class);
        if (alerta.clienteId() != null) {
            service.registrarAlerta(alerta.clienteId());
        }
        log.warn("ALERTA DE SEGURIDAD [{}] cliente {} cuenta {}: {}",
                alerta.tipoAlerta(), alerta.clienteId(), alerta.cuentaId(), alerta.detalle());
    }
}
