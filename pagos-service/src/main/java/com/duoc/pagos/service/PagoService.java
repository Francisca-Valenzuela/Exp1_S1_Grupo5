package com.duoc.pagos.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.duoc.pagos.client.CuentasClient;
import com.duoc.pagos.domain.Pago;
import com.duoc.pagos.dto.PagoResponse;
import com.duoc.pagos.dto.SolicitudPagoRequest;
import com.duoc.pagos.kafka.AlertaSeguridad;
import com.duoc.pagos.kafka.OperacionResultado;
import com.duoc.pagos.kafka.PagoPublisher;
import com.duoc.pagos.kafka.Topics;
import com.duoc.pagos.kafka.TransaccionCompletada;
import com.duoc.pagos.repository.PagoRepository;

/**
 * Procesamiento de pagos, transferencias y depositos como saga coreografiada sobre Kafka:
 *  1) pagos-service guarda el pago PENDIENTE y publica pagos.solicitados (con outbox).
 *  2) cuentas-service aplica el movimiento (transaccion local, idempotente) y publica pagos.resultado.
 *  3) pagos-service actualiza el estado y publica transacciones.completadas / alertas.seguridad.
 * No existe una transaccion distribuida: la consistencia es eventual, con idempotencia y reintentos.
 */
@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository repository;
    private final CuentasClient cuentasClient;
    private final PagoPublisher publisher;
    private final BigDecimal umbralAlerta;

    public PagoService(PagoRepository repository, CuentasClient cuentasClient, PagoPublisher publisher,
                       @Value("${pagos.alerta.monto-umbral:1000000}") BigDecimal umbralAlerta) {
        this.repository = repository;
        this.cuentasClient = cuentasClient;
        this.publisher = publisher;
        this.umbralAlerta = umbralAlerta;
    }

    public PagoResponse solicitar(String tipo, SolicitudPagoRequest req, String canal, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Pago> existente = repository.findByIdempotencyKey(idempotencyKey);
            if (existente.isPresent()) {
                return PagoResponse.desde(existente.get());   // reintento del cliente: no se duplica
            }
        }
        validar(tipo, req);
        validarCuentas(tipo, req);

        Pago pago = new Pago();
        pago.setSolicitudId(UUID.randomUUID().toString());
        pago.setTipo(tipo);
        pago.setCuentaOrigenId(req.cuentaOrigenId());
        pago.setCuentaDestinoId(req.cuentaDestinoId());
        pago.setMonto(req.monto());
        pago.setDescripcion(req.descripcion());
        pago.setCanal(canal);
        pago.setEstado("PENDIENTE");
        pago.setIdempotencyKey(idempotencyKey == null || idempotencyKey.isBlank() ? null : idempotencyKey);
        pago.setCreado(Instant.now());
        pago.setActualizado(pago.getCreado());
        repository.save(pago);   // 1) se persiste primero (outbox)

        enviarSolicitud(pago);    // 2) se intenta publicar; si falla, el scheduler lo reintenta
        return PagoResponse.desde(pago);
    }

    /** Publica una solicitud pendiente y la marca como enviada. Usado por la API y por el outbox. */
    public void enviarSolicitud(Pago pago) {
        if (publisher.publicarSolicitud(pago)) {
            repository.marcarEnviado(pago.getSolicitudId());
        }
    }

    public PagoResponse obtener(String solicitudId) {
        return PagoResponse.desde(buscar(solicitudId));
    }

    public List<PagoResponse> listarPorCuenta(Long cuentaId) {
        return repository.buscarPorCuenta(cuentaId).stream().map(PagoResponse::desde).toList();
    }

    /** Aplica el resultado de cuentas-service y publica los eventos de salida (at-least-once). */
    public void registrarResultado(OperacionResultado r) {
        Optional<Pago> opt = repository.findById(r.solicitudId());
        if (opt.isEmpty()) {
            log.warn("Resultado para solicitud desconocida {}: se ignora", r.solicitudId());
            return;
        }
        Pago pago = opt.get();
        if ("PENDIENTE".equals(pago.getEstado())) {
            boolean aplicado = "APLICADO".equals(r.estado());
            pago.setEstado(aplicado ? "COMPLETADO" : "RECHAZADO");
            pago.setMotivo(r.motivo());
            pago.setClienteId(r.clienteId());
            pago.setEnviado(true);
            pago.setActualizado(Instant.now());
            repository.save(pago);
            log.info("Pago {} -> {}", pago.getSolicitudId(), pago.getEstado());
        }
        // Si la publicacion falla, el mensaje se reintenta y aqui se vuelve a intentar (eventosPublicados=false)
        if (!pago.isEventosPublicados()) {
            publicarEventos(pago);
            repository.marcarEventosPublicados(pago.getSolicitudId());
        }
    }

    private void publicarEventos(Pago pago) {
        Long cuentaPrincipal = pago.getCuentaOrigenId() != null ? pago.getCuentaOrigenId() : pago.getCuentaDestinoId();

        if ("COMPLETADO".equals(pago.getEstado())) {
            publisher.publicarEvento(Topics.TRANSACCIONES_COMPLETADAS, pago.getSolicitudId(),
                    new TransaccionCompletada(UUID.randomUUID().toString(), pago.getSolicitudId(), pago.getTipo(),
                            pago.getCuentaOrigenId(), pago.getCuentaDestinoId(), pago.getClienteId(),
                            pago.getMonto(), pago.getCanal(), Instant.now()));

            if (pago.getMonto().compareTo(umbralAlerta) >= 0) {
                alerta(pago, cuentaPrincipal, "MONTO_ALTO",
                        "Operacion de " + pago.getMonto() + " supera el umbral de " + umbralAlerta);
            }
        } else {
            alerta(pago, cuentaPrincipal, "OPERACION_RECHAZADA", pago.getMotivo());
        }
    }

    private void alerta(Pago pago, Long cuentaId, String tipoAlerta, String detalle) {
        publisher.publicarEvento(Topics.ALERTAS_SEGURIDAD, String.valueOf(cuentaId),
                new AlertaSeguridad(UUID.randomUUID().toString(), tipoAlerta, pago.getClienteId(), cuentaId,
                        pago.getMonto(), detalle, Instant.now()));
    }

    private void validar(String tipo, SolicitudPagoRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Solicitud vacia");
        }
        if (req.monto() == null || req.monto().signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        switch (tipo) {
            case "PAGO" -> requerir(req.cuentaOrigenId(), "cuentaOrigenId");
            case "DEPOSITO" -> requerir(req.cuentaDestinoId(), "cuentaDestinoId");
            case "TRANSFERENCIA" -> {
                requerir(req.cuentaOrigenId(), "cuentaOrigenId");
                requerir(req.cuentaDestinoId(), "cuentaDestinoId");
                if (req.cuentaOrigenId().equals(req.cuentaDestinoId())) {
                    throw new ReglaNegocioException("La cuenta origen y destino no pueden ser la misma");
                }
            }
            default -> throw new IllegalArgumentException("Tipo no soportado: " + tipo);
        }
    }

    private void validarCuentas(String tipo, SolicitudPagoRequest req) {
        for (Long id : new Long[] {req.cuentaOrigenId(), req.cuentaDestinoId()}) {
            if (id != null && Boolean.FALSE.equals(cuentasClient.existe(id))) {
                throw new ReglaNegocioException("La cuenta " + id + " no existe");
            }
        }
    }

    private void requerir(Long valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
    }

    private Pago buscar(String solicitudId) {
        return repository.findById(solicitudId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud " + solicitudId + " no encontrada"));
    }
}
