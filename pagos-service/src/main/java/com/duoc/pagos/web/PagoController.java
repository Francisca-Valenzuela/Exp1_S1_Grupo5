package com.duoc.pagos.web;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.pagos.dto.PagoResponse;
import com.duoc.pagos.dto.SolicitudPagoRequest;
import com.duoc.pagos.service.PagoService;

/** Las operaciones son asincronas: responden 202 Accepted y el estado final se consulta por solicitudId. */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @PostMapping("/pagos")
    public ResponseEntity<PagoResponse> pagar(@RequestBody SolicitudPagoRequest request,
                                              @RequestHeader(value = "Idempotency-Key", required = false) String clave,
                                              Authentication auth) {
        return aceptado(service.solicitar("PAGO", request, canal(auth), clave));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<PagoResponse> transferir(@RequestBody SolicitudPagoRequest request,
                                                   @RequestHeader(value = "Idempotency-Key", required = false) String clave,
                                                   Authentication auth) {
        return aceptado(service.solicitar("TRANSFERENCIA", request, canal(auth), clave));
    }

    @PostMapping("/depositos")
    public ResponseEntity<PagoResponse> depositar(@RequestBody SolicitudPagoRequest request,
                                                  @RequestHeader(value = "Idempotency-Key", required = false) String clave,
                                                  Authentication auth) {
        return aceptado(service.solicitar("DEPOSITO", request, canal(auth), clave));
    }

    @GetMapping("/{solicitudId}")
    public ResponseEntity<PagoResponse> obtener(@PathVariable String solicitudId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.obtener(solicitudId));
    }

    @GetMapping
    public List<PagoResponse> listarPorCuenta(@RequestParam Long cuentaId) {
        return service.listarPorCuenta(cuentaId);
    }

    private ResponseEntity<PagoResponse> aceptado(PagoResponse r) {
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore()).body(r);
    }

    /** El canal se deduce del scope del token (web, mobile, atm o internal). */
    private String canal(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("SCOPE_"))
                .map(a -> a.substring("SCOPE_".length()))
                .findFirst().orElse("desconocido");
    }
}
