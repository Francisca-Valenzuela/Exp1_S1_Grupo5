package com.duoc.bancoxyzbatch.bff.atm;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyzbatch.bff.dto.CuentaAtmDTO;
import com.duoc.bancoxyzbatch.bff.dto.RetiroRequestDTO;
import com.duoc.bancoxyzbatch.bff.dto.RetiroResponseDTO;

@RestController
@RequestMapping("/api/atm")
public class AtmBffController {

    private final AtmBffService atmBffService;

    public AtmBffController(AtmBffService atmBffService) {
        this.atmBffService = atmBffService;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<CuentaAtmDTO> consultarSaldo(@PathVariable Long cuentaId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(atmBffService.consultarSaldo(cuentaId));
    }

    @PostMapping("/cuentas/{cuentaId}/retiro")
    public ResponseEntity<RetiroResponseDTO> retirar(@PathVariable Long cuentaId,
                                                       @RequestBody RetiroRequestDTO request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(atmBffService.retirar(cuentaId, request.getMonto()));
    }
}