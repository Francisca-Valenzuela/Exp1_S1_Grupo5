package com.duoc.pagos.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.duoc.pagos.service.PagoService;

/** Consume el resultado que publica cuentas-service (grupo "pagos-service"). */
@Component
public class PagosListener {

    private final EventoJson json;
    private final PagoService service;

    public PagosListener(EventoJson json, PagoService service) {
        this.json = json;
        this.service = service;
    }

    @KafkaListener(topics = Topics.PAGOS_RESULTADO)
    public void onResultado(String payload) {
        service.registrarResultado(json.fromJson(payload, OperacionResultado.class));
    }
}
