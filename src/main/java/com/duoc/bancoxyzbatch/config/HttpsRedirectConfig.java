package com.duoc.bancoxyzbatch.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fuerza HTTPS de manera uniforme en los 3 BFF (Web, Móvil, Cajero).
 *
 * El servidor principal (Tomcat embebido) sirve HTTPS en el puerto 8443
 * (configurado en application.properties, server.ssl.*). Este bean agrega
 * un segundo conector HTTP en el puerto 8080 cuya única función es
 * redirigir toda solicitud hacia el conector seguro — así, aunque un
 * cliente llame por error a http://, nunca llega a intercambiar datos
 * sin cifrar con ninguno de los tres canales.
 */
@Configuration
public class HttpsRedirectConfig {

    private static final int HTTP_PORT = 8080;
    private static final int HTTPS_PORT = 8443;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpToHttpsRedirectConnector() {
        return factory -> {
            Connector httpConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            httpConnector.setScheme("http");
            httpConnector.setPort(HTTP_PORT);
            httpConnector.setSecure(false);
            httpConnector.setRedirectPort(HTTPS_PORT);
            factory.addAdditionalConnectors(httpConnector);
        };
    }
}