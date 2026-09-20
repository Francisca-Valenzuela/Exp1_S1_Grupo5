package com.duoc.bancoxyzbatch.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyzbatch.internal.dto.TransaccionInternalDTO;

@RestController
@RequestMapping("/internal/transacciones")
public class InternalTransaccionController {

    private final InternalCuentaService internalCuentaService;

    public InternalTransaccionController(InternalCuentaService internalCuentaService) {
        this.internalCuentaService = internalCuentaService;
    }

    @GetMapping
    public ResponseEntity<Page<TransaccionInternalDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(internalCuentaService.listarTransacciones(pageable));
    }
}
