package com.duoc.bancoxyzbatch.bff.mobile;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyzbatch.bff.dto.CuentaMobileDTO;

/**
 * BFF Móvil: respuestas livianas y esenciales, pensadas para reducir
 * el consumo de ancho de banda y mejorar la velocidad en la app.
 */
@RestController
@RequestMapping("/api/mobile")
public class MobileBffController {

    private final MobileBffService mobileBffService;

    public MobileBffController(MobileBffService mobileBffService) {
        this.mobileBffService = mobileBffService;
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaMobileDTO> obtenerCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(mobileBffService.obtenerCuenta(cuentaId));
    }
}