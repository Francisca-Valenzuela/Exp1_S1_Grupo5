package com.duoc.bancoxyzbatch.bff.security;

/**
 * Credenciales de canal usadas para obtener un token JWT en /api/auth/login.
 * Son las mismas credenciales por canal ya definidas en SecurityConfig
 * (web-client/web-secret, mobile-client/mobile-secret, atm-client/atm-secret).
 */
public class LoginRequestDTO {

    private String username;
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}