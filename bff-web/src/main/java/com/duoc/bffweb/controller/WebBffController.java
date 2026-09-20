package com.duoc.bffweb.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bffweb.dto.CuentaWebDTO;
import com.duoc.bffweb.dto.TransaccionWebDTO;
import com.duoc.bffweb.service.WebBffService;

@RestController
@RequestMapping("/api/web")
public class WebBffController {

    private final WebBffService webBffService;

    public WebBffController(WebBffService webBffService) {
        this.webBffService = webBffService;
    }

    @GetMapping("/transacciones")
    public ResponseEntity<Page<TransaccionWebDTO>> listarTransacciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TransaccionWebDTO> resultado = webBffService.listarTransacciones(PageRequest.of(page, size));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(resultado);
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaWebDTO> obtenerCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(webBffService.obtenerCuenta(cuentaId));
    }
}
