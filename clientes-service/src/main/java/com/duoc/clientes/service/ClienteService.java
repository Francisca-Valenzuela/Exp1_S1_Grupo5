package com.duoc.clientes.service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.clientes.domain.Cliente;
import com.duoc.clientes.dto.ClienteRequest;
import com.duoc.clientes.dto.ClienteResponse;
import com.duoc.clientes.kafka.CuentaMigrada;
import com.duoc.clientes.repository.ClienteRepository;

/** Informacion personal y perfiles de clientes. */
@Service
public class ClienteService {

    private static final int EDAD_MINIMA = 18;   // misma regla que el job batch de intereses
    private static final Set<String> PERFILES = Set.of("BASICO", "PREMIUM");

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public ClienteResponse crear(ClienteRequest req) {
        validar(req);
        Cliente c = new Cliente();
        aplicar(c, req);
        c.setPerfil("BASICO");
        c.setEstado("ACTIVO");
        c.setFechaRegistro(Instant.now());
        c.setOrigen("API");
        return ClienteResponse.desde(repository.save(c));
    }

    public ClienteResponse obtener(Long id) {
        return ClienteResponse.desde(buscar(id));
    }

    public Page<ClienteResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(ClienteResponse::desde);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        validar(req);
        Cliente c = buscar(id);
        if ("INACTIVO".equals(c.getEstado())) {
            throw new ReglaNegocioException("El cliente " + id + " esta dado de baja");
        }
        aplicar(c, req);
        return ClienteResponse.desde(repository.save(c));
    }

    @Transactional
    public ClienteResponse cambiarPerfil(Long id, String perfil) {
        if (perfil == null || !PERFILES.contains(perfil.toUpperCase())) {
            throw new IllegalArgumentException("perfil invalido; valores permitidos: " + PERFILES);
        }
        Cliente c = buscar(id);
        c.setPerfil(perfil.toUpperCase());
        return ClienteResponse.desde(repository.save(c));
    }

    /** Baja logica: se conserva el historial del cliente. */
    @Transactional
    public ClienteResponse darDeBaja(Long id) {
        Cliente c = buscar(id);
        c.setEstado("INACTIVO");
        return ClienteResponse.desde(repository.save(c));
    }

    public boolean registrarMigrado(CuentaMigrada evento) {
        Long id = evento.clienteId() != null ? evento.clienteId() : evento.cuentaId();
        if (id == null) {
            throw new IllegalArgumentException("Evento CuentaMigrada sin identificador");
        }
        String nombre = evento.nombre() == null || evento.nombre().isBlank() ? "Sin nombre" : evento.nombre();
        return repository.insertarMigradoSiNoExiste(id, nombre, evento.edad()) > 0;
    }

    public void registrarAlerta(Long clienteId) {
        repository.registrarAlerta(clienteId);
    }

    public void registrarOperacion(Long clienteId) {
        repository.registrarOperacion(clienteId);
    }

    private void validar(ClienteRequest req) {
        if (req == null || req.nombre() == null || req.nombre().isBlank()) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
        if (req.edad() == null || req.edad() < EDAD_MINIMA) {
            throw new ReglaNegocioException("El cliente debe ser mayor de " + EDAD_MINIMA + " anos");
        }
        if (req.email() != null && !req.email().isBlank() && !req.email().contains("@")) {
            throw new IllegalArgumentException("email invalido");
        }
    }

    private void aplicar(Cliente c, ClienteRequest req) {
        c.setNombre(req.nombre().trim());
        c.setEdad(req.edad());
        c.setEmail(req.email());
        c.setTelefono(req.telefono());
    }

    private Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente " + id + " no encontrado"));
    }
}
