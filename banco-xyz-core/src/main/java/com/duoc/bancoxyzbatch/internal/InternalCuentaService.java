package com.duoc.bancoxyzbatch.internal;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.internal.dto.CuentaInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.MovimientoInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.RetiroResultInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.TransaccionInternalDTO;
import com.duoc.bancoxyzbatch.internal.exception.SaldoInsuficienteException;
import com.duoc.bancoxyzbatch.repository.CuentaAnualRepository;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;
import com.duoc.bancoxyzbatch.repository.TransaccionRepository;

/**
 * Logica de negocio de cuentas/transacciones, ahora expuesta como API
 * interna (antes vivia repartida dentro de WebBffService, MobileBffService y
 * AtmBffService). bff-web, bff-mobile y bff-atm consumen este servicio via
 * HTTP (InternalCuentaController) y adaptan la respuesta a su canal.
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

    @Transactional
    public RetiroResultInternalDTO retirar(Long cuentaId, Double monto) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a cero");
        }

        CuentaInteresEntity cuenta = buscarCuenta(cuentaId);

        if (cuenta.getSaldoFinal() < monto) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente en la cuenta " + cuentaId + " para retirar " + monto);
        }

        cuenta.setSaldoFinal(cuenta.getSaldoFinal() - monto);
        cuentaInteresRepository.save(cuenta);

        return new RetiroResultInternalDTO(cuenta.getCuentaId(), monto, cuenta.getSaldoFinal());
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
