package com.duoc.bancoxyzbatch.internal;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Autenticacion de servicio a servicio para /internal/**.
 * Cada BFF (web, mobile, atm) envia el header X-Internal-Api-Key con el
 * valor que tambien recibe desde config-server (bff.jwt/internal.api-key en
 * application.yml comun). Si no coincide, se corta la peticion con 401
 * antes de llegar al controller.
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Api-Key";

    private final String expectedApiKey;

    public InternalApiKeyFilter(@Value("${internal.api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/internal/")) {
            String provided = request.getHeader(HEADER);

            if (provided == null || !provided.equals(expectedApiKey)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API key interna invalida o ausente");
                return;
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    "internal-service", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
