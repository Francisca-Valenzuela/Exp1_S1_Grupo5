package com.duoc.bancoxyzbatch.bff.mobile;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyzbatch.bff.dto.CuentaMobileDTO;
import com.duoc.bancoxyzbatch.bff.dto.MovimientoMobileDTO;
import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.repository.CuentaAnualRepository;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;

@Service
public class MobileBffService {

    // Cantidad de movimientos recientes que se envían al móvil,
    // para no sobrecargar la app con el historial completo del año.
    private static final int MAX_MOVIMIENTOS_RECIENTES = 5;

    private final CuentaInteresRepository cuentaInteresRepository;
    private final CuentaAnualRepository cuentaAnualRepository;

    public MobileBffService(CuentaInteresRepository cuentaInteresRepository,
                             CuentaAnualRepository cuentaAnualRepository) {
        this.cuentaInteresRepository = cuentaInteresRepository;
        this.cuentaAnualRepository = cuentaAnualRepository;
    }

    public CuentaMobileDTO obtenerCuenta(Long cuentaId) {
        CuentaInteresEntity cuenta = cuentaInteresRepository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta " + cuentaId + " no encontrada"));

        List<MovimientoMobileDTO> recientes = cuentaAnualRepository.findByCuentaId(cuentaId).stream()
                .sorted(Comparator.comparing(CuentaAnualEntity::getFecha).reversed())
                .limit(MAX_MOVIMIENTOS_RECIENTES)
                .map(m -> new MovimientoMobileDTO(m.getFecha(), m.getMonto(), m.getDescripcion()))
                .collect(Collectors.toList());

        return new CuentaMobileDTO(
                cuenta.getCuentaId(),
                cuenta.getNombre(),
                cuenta.getSaldoFinal(),
                recientes
        );
    }
}