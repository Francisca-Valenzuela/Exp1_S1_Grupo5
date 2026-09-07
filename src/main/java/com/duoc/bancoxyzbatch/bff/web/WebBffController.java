package com.duoc.bancoxyzbatch.bff.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyzbatch.bff.dto.CuentaWebDTO;
import com.duoc.bancoxyzbatch.bff.dto.TransaccionWebDTO;

/**
 * BFF Web: optimizado para navegadores. Entrega datos completos
 * (incluida la anomalía de cada transacción y el historial completo
 * de movimientos por cuenta) para soportar interfaces ricas
 * (tablas, gráficos, auditoría).
 */
@RestController
@RequestMapping("/api/web")
public class WebBffController {

    private final WebBffService webBffService;

    public WebBffController(WebBffService webBffService) {
        this.webBffService = webBffService;
    }

    @GetMapping("/transacciones")
    public ResponseEntity<List<TransaccionWebDTO>> listarTransacciones() {
        return ResponseEntity.ok(webBffService.listarTransacciones());
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaWebDTO> obtenerCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(webBffService.obtenerCuenta(cuentaId));
    }
}