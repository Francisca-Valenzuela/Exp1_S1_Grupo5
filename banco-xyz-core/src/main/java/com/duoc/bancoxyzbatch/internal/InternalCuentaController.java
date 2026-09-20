package com.duoc.bancoxyzbatch.internal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyzbatch.internal.dto.CuentaInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.RetiroInternalDTO;
import com.duoc.bancoxyzbatch.internal.dto.RetiroResultInternalDTO;

/**
 * API interna de banco-xyz-core: NO la consume el usuario final, sino
 * bff-web, bff-mobile y bff-atm (descubiertos via Eureka), protegidos por
 * InternalApiKeyFilter. Es el resultado de la migracion de datos legacy
 * (Spring Batch) expuesto como API, tal como pide el enunciado.
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

    @PostMapping("/{cuentaId}/retiro")
    public ResponseEntity<RetiroResultInternalDTO> retirar(@PathVariable Long cuentaId,
                                                            @RequestBody RetiroInternalDTO request) {
        return ResponseEntity.ok(internalCuentaService.retirar(cuentaId, request.getMonto()));
    }
}
