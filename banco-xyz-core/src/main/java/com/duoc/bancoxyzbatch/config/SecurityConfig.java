package com.duoc.bancoxyzbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.duoc.bancoxyzbatch.internal.InternalApiKeyFilter;

/**
 * banco-xyz-core es un microservicio interno: no atiende usuarios finales
 * directamente (eso lo hacen bff-web, bff-mobile y bff-atm, cada uno con su
 * propia autenticacion JWT por canal). Aun asi, sus endpoints /internal/**
 * quedan protegidos por una clave de servicio a servicio para que solo los
 * BFF autorizados puedan consumirlos, aunque queden expuestos en la red.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            InternalApiKeyFilter internalApiKeyFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/internal/**").permitAll() // filtrado real por InternalApiKeyFilter
                .anyRequest().authenticated()
            )
            .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
