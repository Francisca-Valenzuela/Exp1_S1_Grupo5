package com.duoc.bffmobile.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bffmobile.dto.CuentaMobileDTO;
import com.duoc.bffmobile.service.MobileBffService;

@RestController
@RequestMapping("/api/mobile")
public class MobileBffController {

    private final MobileBffService mobileBffService;

    public MobileBffController(MobileBffService mobileBffService) {
        this.mobileBffService = mobileBffService;
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaMobileDTO> obtenerCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(15, TimeUnit.SECONDS))
                .body(mobileBffService.obtenerCuenta(cuentaId));
    }
}
