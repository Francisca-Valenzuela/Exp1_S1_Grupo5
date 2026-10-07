package com.duoc.cuentas.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.duoc.cuentas.service.CuentaService;
import com.duoc.cuentas.service.OperacionService;

/**
 * Consumidores de cuentas-service. Todos pertenecen al grupo "cuentas-service": si se levantan
 * varias replicas, Kafka reparte las particiones entre ellas (competing consumers, sin duplicar trabajo).
 */
@Component
public class CuentasListeners {

    private static final Logger log = LoggerFactory.getLogger(CuentasListeners.class);

    private final EventoJson json;
    private final CuentaService cuentaService;
    private final OperacionService operacionService;
    private final EventoPublisher publisher;

    public CuentasListeners(EventoJson json, CuentaService cuentaService,
                            OperacionService operacionService, EventoPublisher publisher) {
        this.json = json;
        this.cuentaService = cuentaService;
        this.operacionService = operacionService;
        this.publisher = publisher;
    }

    /** Carga inicial: cuentas migradas desde el legacy por el job de intereses del core. */
    @KafkaListener(topics = Topics.CUENTAS_MIGRADAS)
    public void onCuentaMigrada(String payload) {
        CuentaMigrada evento = json.fromJson(payload, CuentaMigrada.class);
        boolean nueva = cuentaService.registrarMigrada(evento);
        log.info("CuentaMigrada {} -> {}", evento.cuentaId(), nueva ? "creada" : "ya existia (se conserva)");
    }

    @KafkaListener(topics = Topics.PAGOS_SOLICITADOS)
    public void onPagoSolicitado(String payload) {
        procesar(payload, Topics.PAGOS_RESULTADO);
    }

    @KafkaListener(topics = Topics.RETIROS_SOLICITADOS)
    public void onRetiroSolicitado(String payload) {
        procesar(payload, Topics.RETIROS_RESULTADO);
    }

    private void procesar(String payload, String topicoResultado) {
        OperacionSolicitada op = json.fromJson(payload, OperacionSolicitada.class);
        log.info("Operacion {} ({}) recibida por canal {}", op.solicitudId(), op.tipo(), op.canal());

        // 1) transaccion local en BD (idempotente)   2) publicar el resultado.
        // Si (2) falla, el mensaje se reintenta y (1) devuelve el resultado ya guardado: no se duplica el movimiento.
        OperacionResultado resultado = operacionService.aplicar(op);
        publisher.publicar(topicoResultado, op.solicitudId(), resultado);
    }
}
