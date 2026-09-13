package com.duoc.bancoxyzbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad por canal (patrón BFF).
 * Cada BFF (Web, Móvil, Cajero) tiene su propio rol y sus propios
 * endpoints, de modo que un cliente autenticado para un canal no puede
 * acceder a los endpoints de otro canal.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails webClient = User.builder()
                .username("web-client")
                .password(encoder.encode("web-secret"))
                .roles("WEB")
                .build();

        UserDetails mobileClient = User.builder()
                .username("mobile-client")
                .password(encoder.encode("mobile-secret"))
                .roles("MOBILE")
                .build();

        UserDetails atmClient = User.builder()
                .username("atm-client")
                .password(encoder.encode("atm-secret"))
                .roles("ATM")
                .build();

        return new InMemoryUserDetailsManager(webClient, mobileClient, atmClient);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // APIs stateless, sin formularios
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/web/**").hasRole("WEB")
                .requestMatchers("/api/mobile/**").hasRole("MOBILE")
                .requestMatchers("/api/atm/**").hasRole("ATM")
                .requestMatchers("/h2-console/**").permitAll() // conservar acceso a H2 console
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}) // Basic Auth simple, suficiente para la actividad
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // para H2 console

        return http.build();
    }
}