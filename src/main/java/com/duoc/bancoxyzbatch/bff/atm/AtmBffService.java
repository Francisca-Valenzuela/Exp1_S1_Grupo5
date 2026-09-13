package com.duoc.bancoxyzbatch.bff.atm;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.bancoxyzbatch.bff.dto.CuentaAtmDTO;
import com.duoc.bancoxyzbatch.bff.dto.RetiroResponseDTO;
import com.duoc.bancoxyzbatch.bff.exception.SaldoInsuficienteException;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;

@Service
public class AtmBffService {

    private final CuentaInteresRepository cuentaInteresRepository;

    public AtmBffService(CuentaInteresRepository cuentaInteresRepository) {
        this.cuentaInteresRepository = cuentaInteresRepository;
    }

    public CuentaAtmDTO consultarSaldo(Long cuentaId) {
        CuentaInteresEntity cuenta = buscarCuenta(cuentaId);
        return new CuentaAtmDTO(cuenta.getCuentaId(), cuenta.getSaldoFinal());
    }

    @Transactional
    public RetiroResponseDTO retirar(Long cuentaId, Double monto) {
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

        return new RetiroResponseDTO(cuenta.getCuentaId(), monto, cuenta.getSaldoFinal());
    }

    private CuentaInteresEntity buscarCuenta(Long cuentaId) {
        return cuentaInteresRepository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta " + cuentaId + " no encontrada"));
    }
}