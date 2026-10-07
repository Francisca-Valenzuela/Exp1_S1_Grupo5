package com.duoc.clientes.web;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.clientes.dto.ClienteRequest;
import com.duoc.clientes.dto.ClienteResponse;
import com.duoc.clientes.dto.PerfilRequest;
import com.duoc.clientes.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@RequestBody ClienteRequest request) {
        ClienteResponse creado = service.crear(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + creado.clienteId())).body(creado);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public Page<ClienteResponse> listar(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return service.listar(PageRequest.of(page, Math.min(size, 100)));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    @PutMapping("/{id}/perfil")
    public ClienteResponse cambiarPerfil(@PathVariable Long id, @RequestBody PerfilRequest request) {
        return service.cambiarPerfil(id, request.perfil());
    }

    @DeleteMapping("/{id}")
    public ClienteResponse darDeBaja(@PathVariable Long id) {
        return service.darDeBaja(id);
    }
}
