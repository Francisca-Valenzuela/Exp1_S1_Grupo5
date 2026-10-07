package com.duoc.cuentas.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.cuentas.domain.Cuenta;
import com.duoc.cuentas.domain.EstadoCuenta;
import com.duoc.cuentas.domain.OperacionProcesada;
import com.duoc.cuentas.kafka.OperacionResultado;
import com.duoc.cuentas.kafka.OperacionSolicitada;
import com.duoc.cuentas.repository.CuentaRepository;
import com.duoc.cuentas.repository.OperacionProcesadaRepository;

/**
 * Aplica movimientos de dinero (pago, transferencia, deposito, retiro) en UNA transaccion local
 * de base de datos, con dos garantias de consistencia:
 *  1) Idempotencia: la misma solicitudId nunca se aplica dos veces (tabla operaciones_procesadas).
 *  2) Bloqueo pesimista de las cuentas involucradas (en orden de id, para evitar deadlocks).
 * Una transferencia es atomica porque ambas cuentas viven en este mismo servicio.
 */
@Service
public class OperacionService {

    private static final Logger log = LoggerFactory.getLogger(OperacionService.class);

    private final CuentaRepository cuentas;
    private final OperacionProcesadaRepository procesadas;

    public OperacionService(CuentaRepository cuentas, OperacionProcesadaRepository procesadas) {
        this.cuentas = cuentas;
        this.procesadas = procesadas;
    }

    @Transactional
    public OperacionResultado aplicar(OperacionSolicitada op) {
        Optional<OperacionProcesada> previa = procesadas.findById(op.solicitudId());
        if (previa.isPresent()) {
            log.info("Solicitud {} ya procesada: se reenvia el resultado almacenado (idempotencia)", op.solicitudId());
            return aResultado(previa.get());
        }

        OperacionProcesada registro = new OperacionProcesada();
        registro.setSolicitudId(op.solicitudId());
        registro.setTipo(op.tipo());
        registro.setCuentaOrigenId(op.cuentaOrigenId());
        registro.setCuentaDestinoId(op.cuentaDestinoId());
        registro.setMonto(op.monto());
        registro.setFecha(Instant.now());

        try {
            ejecutar(op, registro);
            registro.setEstado("APLICADO");
            registro.setMotivo("Operacion aplicada");
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            // Rechazo de NEGOCIO: no se reintenta, se informa el motivo. No hubo cambios que revertir.
            registro.setEstado("RECHAZADO");
            registro.setMotivo(e.getMessage());
            log.warn("Solicitud {} rechazada: {}", op.solicitudId(), e.getMessage());
        }
        procesadas.save(registro);
        return aResultado(registro);
    }

    private void ejecutar(OperacionSolicitada op, OperacionProcesada registro) {
        if (op.tipo() == null) {
            throw new IllegalArgumentException("tipo de operacion obligatorio");
        }
        if (op.monto() == null || op.monto().signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        switch (op.tipo()) {
            case "DEPOSITO" -> {
                requerir(op.cuentaDestinoId(), "cuentaDestinoId");
                Cuenta destino = bloquear(op.cuentaDestinoId());
                registro.setClienteId(destino.getClienteId());
                acreditar(destino, op.monto());
                registro.setSaldoOrigen(destino.getSaldo());
            }
            case "RETIRO", "PAGO" -> {
                requerir(op.cuentaOrigenId(), "cuentaOrigenId");
                Cuenta origen = bloquear(op.cuentaOrigenId());
                registro.setClienteId(origen.getClienteId());   // antes de debitar: un rechazo tambien debe identificar al cliente
                debitar(origen, op.monto());
                registro.setSaldoOrigen(origen.getSaldo());
            }
            case "TRANSFERENCIA" -> {
                requerir(op.cuentaOrigenId(), "cuentaOrigenId");
                requerir(op.cuentaDestinoId(), "cuentaDestinoId");
                if (op.cuentaOrigenId().equals(op.cuentaDestinoId())) {
                    throw new ReglaNegocioException("La cuenta origen y destino no pueden ser la misma");
                }
                // bloqueo en orden ascendente de id: dos transferencias cruzadas no se interbloquean
                Set<Long> orden = new TreeSet<>(Set.of(op.cuentaOrigenId(), op.cuentaDestinoId()));
                Cuenta origen = null;
                Cuenta destino = null;
                for (Long id : orden) {
                    Cuenta c = bloquear(id);
                    if (id.equals(op.cuentaOrigenId())) {
                        origen = c;
                    } else {
                        destino = c;
                    }
                }
                registro.setClienteId(origen.getClienteId());
                debitar(origen, op.monto());
                acreditar(destino, op.monto());
                registro.setSaldoOrigen(origen.getSaldo());
            }
            default -> throw new IllegalArgumentException("Tipo de operacion no soportado: " + op.tipo());
        }
    }

    private void requerir(Long valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " es obligatorio para esta operacion");
        }
    }

    private Cuenta bloquear(Long id) {
        Cuenta cuenta = cuentas.buscarParaActualizar(id)
                .orElseThrow(() -> new ReglaNegocioException("Cuenta " + id + " no encontrada"));
        if (cuenta.getEstado() != EstadoCuenta.ABIERTA) {
            throw new ReglaNegocioException("La cuenta " + id + " no esta operativa (estado " + cuenta.getEstado() + ")");
        }
        return cuenta;
    }

    private void debitar(Cuenta cuenta, BigDecimal monto) {
        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new ReglaNegocioException("Saldo insuficiente en la cuenta " + cuenta.getId());
        }
        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuentas.save(cuenta);
    }

    private void acreditar(Cuenta cuenta, BigDecimal monto) {
        cuenta.setSaldo(cuenta.getSaldo().add(monto));
        cuentas.save(cuenta);
    }

    private OperacionResultado aResultado(OperacionProcesada r) {
        return new OperacionResultado(r.getSolicitudId(), r.getTipo(), r.getEstado(), r.getMotivo(),
                r.getCuentaOrigenId(), r.getCuentaDestinoId(), r.getClienteId(), r.getMonto(), r.getSaldoOrigen());
    }
}
