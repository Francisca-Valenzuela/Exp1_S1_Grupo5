package com.duoc.bancoxyzbatch.internal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyzbatch.internal.dto.CuentaInternalDTO;

/**
 * API interna de banco-xyz-core: la consumen bff-web y bff-mobile (descubiertos
 * via Eureka) reenviando el JWT del canal; exige scope web, mobile, atm o
 * internal. Expone el resultado de la migracion de datos legacy (Spring Batch).
 */
@RestController
@RequestMapping("/internal/cuentas")
public class InternalCuentaController {

    private final InternalCuentaService internalCuentaService;

    public InternalCuentaController(InternalCuentaService internalCuentaService) {
        this.internalCuentaService = internalCuentaService;
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaInternalDTO> obtenerCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(internalCuentaService.obtenerCuenta(cuentaId));
    }
}
