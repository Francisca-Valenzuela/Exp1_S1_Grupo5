package com.duoc.cuentas.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.cuentas.client.ClientesClient;
import com.duoc.cuentas.domain.Cuenta;
import com.duoc.cuentas.domain.EstadoCuenta;
import com.duoc.cuentas.dto.AperturaCuentaRequest;
import com.duoc.cuentas.dto.CuentaResponse;
import com.duoc.cuentas.dto.MantenimientoCuentaRequest;
import com.duoc.cuentas.kafka.CuentaMigrada;
import com.duoc.cuentas.repository.CuentaRepository;

/** Apertura, consulta, mantenimiento y cierre de cuentas. */
@Service
public class CuentaService {

    private static final Logger log = LoggerFactory.getLogger(CuentaService.class);

    private final CuentaRepository repository;
    private final ClientesClient clientesClient;

    public CuentaService(CuentaRepository repository, ClientesClient clientesClient) {
        this.repository = repository;
        this.clientesClient = clientesClient;
    }

    public CuentaResponse abrir(AperturaCuentaRequest req) {
        if (req == null || req.clienteId() == null) {
            throw new IllegalArgumentException("clienteId es obligatorio");
        }
        if (req.tipo() == null || req.tipo().isBlank()) {
            throw new IllegalArgumentException("tipo de cuenta es obligatorio");
        }
        BigDecimal saldoInicial = req.saldoInicial() == null ? BigDecimal.ZERO : req.saldoInicial();
        if (saldoInicial.signum() < 0) {
            throw new IllegalArgumentException("saldoInicial no puede ser negativo");
        }

        // Llamada protegida por Resilience4j: TRUE existe, FALSE no existe, null = servicio caido (fallback)
        Boolean clienteExiste = clientesClient.existe(req.clienteId());
        if (Boolean.FALSE.equals(clienteExiste)) {
            throw new ReglaNegocioException("El cliente " + req.clienteId() + " no existe");
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setClienteId(req.clienteId());
        cuenta.setTipo(req.tipo().trim().toLowerCase());
        cuenta.setSaldo(saldoInicial);
        cuenta.setFechaApertura(Instant.now());
        cuenta.setOrigen("API");
        // Comportamiento alternativo: si no se pudo validar al cliente, la cuenta queda pendiente
        cuenta.setEstado(clienteExiste == null ? EstadoCuenta.PENDIENTE_VALIDACION : EstadoCuenta.ABIERTA);

        Cuenta guardada = repository.save(cuenta);
        log.info("Cuenta {} abierta para cliente {} (estado {})",
                guardada.getId(), guardada.getClienteId(), guardada.getEstado());
        return CuentaResponse.desde(guardada);
    }

    public CuentaResponse obtener(Long cuentaId) {
        return CuentaResponse.desde(buscar(cuentaId));
    }

    public List<CuentaResponse> listarPorCliente(Long clienteId) {
        return repository.findByClienteId(clienteId).stream().map(CuentaResponse::desde).toList();
    }

    @Transactional
    public CuentaResponse actualizar(Long cuentaId, MantenimientoCuentaRequest req) {
        Cuenta cuenta = buscar(cuentaId);
        if (cuenta.getEstado() == EstadoCuenta.CERRADA) {
            throw new ReglaNegocioException("La cuenta " + cuentaId + " esta cerrada y no admite cambios");
        }
        if (req == null || req.tipo() == null || req.tipo().isBlank()) {
            throw new IllegalArgumentException("tipo de cuenta es obligatorio");
        }
        cuenta.setTipo(req.tipo().trim().toLowerCase());
        return CuentaResponse.desde(repository.save(cuenta));
    }

    @Transactional
    public CuentaResponse cerrar(Long cuentaId) {
        Cuenta cuenta = repository.buscarParaActualizar(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta " + cuentaId + " no encontrada"));
        if (cuenta.getEstado() == EstadoCuenta.CERRADA) {
            throw new ReglaNegocioException("La cuenta " + cuentaId + " ya esta cerrada");
        }
        if (cuenta.getSaldo().signum() != 0) {
            throw new ReglaNegocioException(
                    "No se puede cerrar la cuenta " + cuentaId + ": el saldo debe ser 0 (actual " + cuenta.getSaldo() + ")");
        }
        cuenta.setEstado(EstadoCuenta.CERRADA);
        return CuentaResponse.desde(repository.save(cuenta));
    }

    /** Carga inicial desde el batch. Idempotente: no pisa cuentas ya existentes. */
    public boolean registrarMigrada(CuentaMigrada evento) {
        if (evento.cuentaId() == null) {
            throw new IllegalArgumentException("Evento CuentaMigrada sin cuentaId");
        }
        int insertadas = repository.insertarMigradaSiNoExiste(
                evento.cuentaId(),
                evento.clienteId() != null ? evento.clienteId() : evento.cuentaId(),
                evento.tipo(),
                evento.saldoFinal() != null ? evento.saldoFinal() : BigDecimal.ZERO);
        return insertadas > 0;
    }

    private Cuenta buscar(Long cuentaId) {
        return repository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta " + cuentaId + " no encontrada"));
    }
}
