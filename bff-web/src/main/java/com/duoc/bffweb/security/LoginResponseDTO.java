package com.duoc.bffweb.security;

public class LoginResponseDTO {

    private String token;
    private String canal;
    private String tipo;

    public LoginResponseDTO(String token, String canal, String tipo) {
        this.token = token;
        this.canal = canal;
        this.tipo = tipo;
    }

    public String getToken() { return token; }
    public String getCanal() { return canal; }
    public String getTipo() { return tipo; }
}
