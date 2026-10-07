package com.duoc.bancoxyzbatch.internal;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.internal.dto.CuentaInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.MovimientoInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.TransaccionInternalDTO;
import com.duoc.bancoxyzbatch.repository.CuentaAnualRepository;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;
import com.duoc.bancoxyzbatch.repository.TransaccionRepository;

/**
 * Consultas de los datos que genera la migracion batch (cuentas con interes,
 * movimientos anuales y reporte de transacciones). La gestion operativa de
 * cuentas (saldos, retiros, apertura, cierre) ahora vive en cuentas-service;
 * este servicio solo entrega los reportes batch a los BFF.
 */
@Service
public class InternalCuentaService {

    private final CuentaInteresRepository cuentaInteresRepository;
    private final CuentaAnualRepository cuentaAnualRepository;
    private final TransaccionRepository transaccionRepository;

    public InternalCuentaService(CuentaInteresRepository cuentaInteresRepository,
                                  CuentaAnualRepository cuentaAnualRepository,
                                  TransaccionRepository transaccionRepository) {
        this.cuentaInteresRepository = cuentaInteresRepository;
        this.cuentaAnualRepository = cuentaAnualRepository;
        this.transaccionRepository = transaccionRepository;
    }

    public CuentaInternalDTO obtenerCuenta(Long cuentaId) {
        CuentaInteresEntity cuenta = buscarCuenta(cuentaId);

        List<MovimientoInternalDTO> movimientos = cuentaAnualRepository.findByCuentaId(cuentaId).stream()
                .map(this::toMovimientoDTO)
                .collect(Collectors.toList());

        return new CuentaInternalDTO(
                cuenta.getCuentaId(),
                cuenta.getNombre(),
                cuenta.getEdad(),
                cuenta.getTipo(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldoFinal(),
                movimientos
        );
    }

    public Page<TransaccionInternalDTO> listarTransacciones(Pageable pageable) {
        return transaccionRepository.findAll(pageable)
                .map(t -> new TransaccionInternalDTO(t.getId(), t.getFecha(), t.getMonto(), t.getTipo(), t.getAnomalia()));
    }

    private CuentaInteresEntity buscarCuenta(Long cuentaId) {
        return cuentaInteresRepository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta " + cuentaId + " no encontrada"));
    }

    private MovimientoInternalDTO toMovimientoDTO(CuentaAnualEntity m) {
        return new MovimientoInternalDTO(m.getId(), m.getFecha(), m.getTransaccion(), m.getMonto(), m.getDescripcion());
    }
}
