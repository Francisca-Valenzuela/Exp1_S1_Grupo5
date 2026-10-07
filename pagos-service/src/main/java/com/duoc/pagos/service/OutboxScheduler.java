package com.duoc.pagos.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.duoc.pagos.domain.Pago;
import com.duoc.pagos.repository.PagoRepository;

/**
 * Outbox: reenvia a Kafka las solicitudes que no pudieron publicarse (broker caido, circuito abierto).
 * Con varias replicas puede enviarse dos veces la misma solicitud; es seguro porque cuentas-service
 * es idempotente por solicitudId.
 */
@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final PagoRepository repository;
    private final PagoService service;

    public OutboxScheduler(PagoRepository repository, PagoService service) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${pagos.outbox.intervalo-ms:5000}")
    public void reenviarPendientes() {
        List<Pago> pendientes = repository.findTop50ByEnviadoFalseOrderByCreadoAsc();
        if (pendientes.isEmpty()) {
            return;
        }
        log.info("Outbox: reintentando {} solicitud(es) sin publicar", pendientes.size());
        pendientes.forEach(service::enviarSolicitud);
    }
}
