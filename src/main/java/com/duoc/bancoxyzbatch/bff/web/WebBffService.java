package com.duoc.bancoxyzbatch.bff.web;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyzbatch.bff.dto.CuentaWebDTO;
import com.duoc.bancoxyzbatch.bff.dto.MovimientoWebDTO;
import com.duoc.bancoxyzbatch.bff.dto.TransaccionWebDTO;
import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.repository.CuentaAnualRepository;
import com.duoc.bancoxyzbatch.repository.CuentaInteresRepository;
import com.duoc.bancoxyzbatch.repository.TransaccionRepository;

@Service
public class WebBffService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaInteresRepository cuentaInteresRepository;
    private final CuentaAnualRepository cuentaAnualRepository;

    public WebBffService(TransaccionRepository transaccionRepository,
                          CuentaInteresRepository cuentaInteresRepository,
                          CuentaAnualRepository cuentaAnualRepository) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaInteresRepository = cuentaInteresRepository;
        this.cuentaAnualRepository = cuentaAnualRepository;
    }

    public List<TransaccionWebDTO> listarTransacciones() {
        return transaccionRepository.findAll().stream()
                .map(t -> new TransaccionWebDTO(t.getId(), t.getFecha(), t.getMonto(), t.getTipo(), t.getAnomalia()))
                .collect(Collectors.toList());
    }

    public CuentaWebDTO obtenerCuenta(Long cuentaId) {
        CuentaInteresEntity cuenta = cuentaInteresRepository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta " + cuentaId + " no encontrada"));

        List<MovimientoWebDTO> historial = cuentaAnualRepository.findByCuentaId(cuentaId).stream()
                .map(this::toMovimientoWebDTO)
                .collect(Collectors.toList());

        return new CuentaWebDTO(
                cuenta.getCuentaId(),
                cuenta.getNombre(),
                cuenta.getEdad(),
                cuenta.getTipo(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldoFinal(),
                historial
        );
    }

    private MovimientoWebDTO toMovimientoWebDTO(CuentaAnualEntity m) {
        return new MovimientoWebDTO(m.getId(), m.getFecha(), m.getTransaccion(), m.getMonto(), m.getDescripcion());
    }
}