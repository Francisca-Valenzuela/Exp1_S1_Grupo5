package com.duoc.cuentas.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.cuentas.dto.AperturaCuentaRequest;
import com.duoc.cuentas.dto.CuentaResponse;
import com.duoc.cuentas.dto.MantenimientoCuentaRequest;
import com.duoc.cuentas.service.CuentaService;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    /** Apertura de cuenta. */
    @PostMapping
    public ResponseEntity<CuentaResponse> abrir(@RequestBody AperturaCuentaRequest request) {
        CuentaResponse creada = service.abrir(request);
        return ResponseEntity.created(URI.create("/api/cuentas/" + creada.cuentaId())).body(creada);
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponse> obtener(@PathVariable Long cuentaId) {
        // el saldo cambia con cada operacion: nunca debe quedar en cache
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.obtener(cuentaId));
    }

    @GetMapping
    public List<CuentaResponse> listarPorCliente(@RequestParam Long clienteId) {
        return service.listarPorCliente(clienteId);
    }

    /** Mantenimiento: actualiza datos de la cuenta (tipo). */
    @PutMapping("/{cuentaId}")
    public CuentaResponse actualizar(@PathVariable Long cuentaId, @RequestBody MantenimientoCuentaRequest request) {
        return service.actualizar(cuentaId, request);
    }

    /** Cierre de cuenta (exige saldo 0). */
    @PostMapping("/{cuentaId}/cierre")
    public CuentaResponse cerrar(@PathVariable Long cuentaId) {
        return service.cerrar(cuentaId);
    }
}
