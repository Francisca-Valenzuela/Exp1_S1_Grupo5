package com.duoc.bancoxyzbatch.bff.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto único de emisión de tokens para los 3 canales del BFF.
 *
 * El cliente (Web, Móvil o Cajero) se autentica una sola vez con sus
 * credenciales de canal y recibe un JWT firmado, que debe reenviar en el
 * header {@code Authorization: Bearer <token>} en cada llamada siguiente a
 * su BFF correspondiente. Este endpoint es el único que queda accesible
 * sin token (permitAll en SecurityConfig); todos los demás exigen JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no tiene rol asignado"));

        String canal = rol.toLowerCase();
        String token = jwtService.generarToken(authentication.getName(), canal, rol);

        return ResponseEntity.ok(new LoginResponseDTO(token, canal, "Bearer"));
    }
}
