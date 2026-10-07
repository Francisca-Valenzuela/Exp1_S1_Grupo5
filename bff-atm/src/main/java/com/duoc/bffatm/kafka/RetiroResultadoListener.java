package com.duoc.bffatm.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.duoc.bffatm.dto.RetiroEstadoDTO;
import com.duoc.bffatm.store.RetiroEstadoStore;

/**
 * Cada replica de bff-atm usa un grupo de consumo PROPIO (UUID) y lee solo mensajes nuevos: asi todas las
 * replicas reciben todos los resultados y pueden responder la consulta del cajero (el estado vive en memoria).
 */
@Component
public class RetiroResultadoListener {

    private static final Logger log = LoggerFactory.getLogger(RetiroResultadoListener.class);

    private final RetiroEstadoStore store;
    private final EventoJson json;

    public RetiroResultadoListener(RetiroEstadoStore store, EventoJson json) {
        this.store = store;
        this.json = json;
    }

    @KafkaListener(topics = Topics.RETIROS_RESULTADO,
            groupId = "bff-atm-#{T(java.util.UUID).randomUUID().toString()}",
            properties = "auto.offset.reset=latest")
    public void recibirResultado(String payload) {
        RetiroResultado r = json.fromJson(payload, RetiroResultado.class);
        String estado = "APLICADO".equals(r.estado()) ? "PROCESADO" : "RECHAZADO";
        store.guardar(new RetiroEstadoDTO(r.solicitudId(), r.cuentaOrigenId(),
                r.monto() == null ? null : r.monto().doubleValue(), estado, r.motivo()));
        log.info("Resultado de retiro recibido. Solicitud: {}, estado: {}", r.solicitudId(), estado);
    }
}
