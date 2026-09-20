# Banco XYZ — Exp 3, Semana 6: Microservicios y seguridad en la nube con Spring Cloud

## Objetivo
Evolucionar el proyecto de Banco XYZ hacia una arquitectura de microservicios
real usando Spring Cloud: configuración centralizada (Config Server),
descubrimiento de servicios (Eureka) y tres microservicios BFF (Web, Móvil,
Cajero) con tolerancia a fallos (Resilience4j) y autenticación JWT.

## Estructura del código
```
eureka-server/     Service Discovery (Eureka), puerto 8761
config-server/     Configuración centralizada (modo native), puerto 8888
banco-xyz-core/    Batch de migración de datos legacy (bank_legacy_data) +
                   API interna /internal/** consumida por los 3 BFF
bff-web/           BFF canal Web,    puerto 8081, JWT + Circuit Breaker
bff-mobile/        BFF canal Móvil,  puerto 8082, JWT + Circuit Breaker
bff-atm/           BFF canal Cajero, puerto 8083, JWT + Circuit Breaker
```

Cada carpeta es un proyecto Maven independiente (jar ejecutable propio),
tal como corresponde a microservicios desplegables por separado.

## Cómo levantar el ecosistema (orden importa)
1. `cd eureka-server && ./mvnw spring-boot:run` → http://localhost:8761
2. `cd config-server && ./mvnw spring-boot:run` → http://localhost:8888
3. Levantar PostgreSQL (docker-compose, igual que semanas anteriores).
4. `cd banco-xyz-core && ./mvnw spring-boot:run` → puerto 8443 (sin TLS,
   ver notas en `config-server/src/main/resources/config-repo/banco-xyz-core.yml`)
5. `cd bff-web && ./mvnw spring-boot:run` → puerto 8081
6. `cd bff-mobile && ./mvnw spring-boot:run` → puerto 8082
7. `cd bff-atm && ./mvnw spring-boot:run` → puerto 8083

Verificar en http://localhost:8761 que las 4 aplicaciones de negocio quedan
**registradas en Eureka**.

## Probar los endpoints (Postman)
1. Login por canal (obtiene JWT):
   - `POST http://localhost:8081/api/auth/login` body `{"username":"web-client","password":"web-secret"}`
   - `POST http://localhost:8082/api/auth/login` body `{"username":"mobile-client","password":"mobile-secret"}`
   - `POST http://localhost:8083/api/auth/login` body `{"username":"atm-client","password":"atm-secret"}`
2. Usar el token recibido como `Authorization: Bearer <token>` en:
   - `GET  http://localhost:8081/api/web/cuentas/{id}`
   - `GET  http://localhost:8081/api/web/transacciones`
   - `GET  http://localhost:8082/api/mobile/cuentas/{id}`
   - `GET  http://localhost:8083/api/atm/cuentas/{id}/saldo`
   - `POST http://localhost:8083/api/atm/cuentas/{id}/retiro` body `{"monto": 1000}`
3. Para evidenciar la tolerancia a fallos: detener `banco-xyz-core` y volver
   a llamar cualquier endpoint de un BFF → responde 503 con mensaje de
   degradación en vez de colgarse (fallback de Resilience4j).

## Componentes por criterio de la pauta
- **Config Server**: `config-server/` sirve `config-repo/*.yml`; lo consumen
  los 4 microservicios de negocio vía `spring.config.import=configserver:`.
- **Service Discovery**: `eureka-server/`; se registran `banco-xyz-core`,
  `bff-web`, `bff-mobile` y `bff-atm` (los 3 exigidos, más el core).
- **3 microservicios con tolerancia a fallos + autenticación**: bff-web,
  bff-mobile y bff-atm, cada uno con `@CircuitBreaker`/`@Retry`
  (Resilience4j) alrededor de la llamada a banco-xyz-core, y JWT propio.
- **Autenticación/autorización funcional**: `AuthController` + `JwtService`
  + `JwtAuthenticationFilter` en cada BFF (roles WEB/MOBILE/ATM), más una
  API key de servicio a servicio en banco-xyz-core (`InternalApiKeyFilter`).

## Evidencia de ejecución
Adjuntar en esta misma carpeta capturas de: los 4 servicios registrados en
Eureka (`localhost:8761`), un login exitoso por canal, una consulta exitosa
por canal, y una llamada fallando en 503 con `banco-xyz-core` detenido
(prueba del Circuit Breaker).
