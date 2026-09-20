package com.duoc.bffweb.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(@Value("${bff.jwt.secret}") String secret,
                       @Value("${bff.jwt.expiration-minutes}") long expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = expirationMinutes * 60_000L;
    }

    public String generarToken(String username, String canal, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("canal", canal)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
