# Banco XYZ - Migración de Procesos Batch con Spring Batch + BFF

## Objetivo del proyecto

Este proyecto moderniza tres procesos batch legacy del **Banco XYZ** (un banco ficticio) utilizando **Spring Batch**, y expone esos datos a través de un **Backend for Frontend (BFF)** adaptado a tres tipos de cliente: Web, Móvil y Cajero Automático. A partir de datos legacy en formato CSV —con problemas típicos de sistemas antiguos como montos negativos, fechas mal formateadas, registros duplicados y campos vacíos—, se implementan tres Jobs que leen, validan/transforman y persisten la información en PostgreSQL, y luego tres APIs REST independientes exponen esa información de forma personalizada según el canal que la consume.

## Procesos batch implementados

| Job | Descripción | Fuente de datos |
|---|---|---|
| `transaccionJob` | Reporte de transacciones diarias: detecta anomalías (montos negativos/cero, duplicados) y genera un resumen | `transacciones.csv` |
| `cuentaInteresJob` | Cálculo de intereses mensuales sobre cuentas de ahorro, préstamo e hipoteca, actualizando el saldo final | `intereses.csv` |
| `cuentaAnualJob` | Generación de estados de cuenta anuales para auditoría | `cuentas_anuales.csv` |

## Estructura del código

```
src/main/java/com/duoc/bancoxyzbatch/
├── BancoXyzBatchApplication.java   # Clase principal
├── BatchRunner.java                # Lanza los 3 Jobs en secuencia al iniciar la app
├── batch/
│   ├── DatoInvalidoException.java  # Excepción para datos irrecuperables (usada por skip)
│   ├── TransaccionReader/Processor/Writer.java
│   ├── CuentaInteresReader/Processor/Writer.java
│   └── CuentaAnualReader/Processor/Writer.java
├── config/
│   ├── HelloWorldJobConfig.java    # Job de prueba inicial
│   ├── TransaccionJobConfig.java   # Job + Step 1
│   ├── CuentaInteresJobConfig.java # Job + Step 2
│   ├── CuentaAnualJobConfig.java   # Job + Step 3
│   ├── SecurityConfig.java         # Autorización por canal + filtro JWT (Exp2 S4/S5)
│   └── HttpsRedirectConfig.java    # Redirige HTTP -> HTTPS de forma uniforme (Exp2 S5)
├── entity/            # Entidades JPA (tablas destino ya validadas)
├── model/              # POJOs de lectura cruda del CSV
├── repository/          # Repositorios Spring Data JPA
└── bff/                 # Backend for Frontend
    ├── dto/              # DTOs por canal (Exp2 S4)
    ├── web/              # WebBffController + WebBffService (Exp2 S4, paginado en S5)
    ├── mobile/           # MobileBffController + MobileBffService (Exp2 S4)
    ├── atm/              # AtmBffController + AtmBffService (Exp2 S4)
    ├── exception/        # Manejo de errores común (Exp2 S4, ahora también login fallido)
    └── security/         # JwtService, JwtAuthenticationFilter, AuthController (Exp2 S5)
```

Cada Job de Batch sigue el patrón estándar de Spring Batch: **ItemReader** (lee el CSV) → **ItemProcessor** (valida, transforma y detecta anomalías) → **ItemWriter** (persiste en PostgreSQL).

> **Nota de implementación:** los beans `*Reader` exponen un `SynchronizedItemStreamReader<X>` (necesario para lectura thread-safe con 3 hilos). Los `*JobConfig` inyectan ese reader directamente como `ItemStreamReader<X>` y lo usan tal cual en `.reader(...)`, sin volver a envolverlo — evita el error `No qualifying bean of type 'FlatFileItemReader'` que aparece si se inyecta el tipo equivocado o se envuelve el reader dos veces.

## Manejo de errores (Batch)

Cada `Step` está configurado con `faultTolerant()`:
- **Skip**: los registros con datos irrecuperables (ej. fechas con formato irreconocible) se omiten sin detener el Job completo, hasta un límite configurable por Step/partición (`batch.skip-limit`, 300 por defecto), gestionado mediante una `CustomSkipPolicy`.
- **Retry**: ante fallos transitorios de conexión a la base de datos, se reintenta hasta 3 veces (`retryLimit`) con una espera creciente entre intentos (`ExponentialBackOffPolicy`) antes de fallar.
- **SkipListener**: cada registro omitido queda registrado en el log con el motivo.

Además, cada `ItemProcessor` detecta y reporta (vía logger) anomalías de calidad de datos que no impiden el guardado (duplicados, edades fuera de rango, etc.). Dos validaciones sí se consideran datos irrecuperables y disparan un skip real (`DatoInvalidoException`):

- `CuentaInteresProcessor`: un `tipo` de cuenta que no sea `ahorro`, `prestamo` o `hipoteca` no tiene tasa de interés definida, por lo que el registro se omite.
- `CuentaAnualProcessor`: un `monto` nulo o en cero no representa un movimiento real, por lo que el registro se omite.

## Novedades Semana 2: procesamiento paralelo y optimización de recursos

- **Procesamiento multihilo (3 hilos por Step)**: `BatchAsyncConfig` define un `TaskExecutor` (`ThreadPoolTaskExecutor`, `corePoolSize`/`maxPoolSize`=3) inyectado en cada `Step`.
- **Lectura thread-safe**: los `Reader` se envuelven con `SynchronizedItemStreamReader`.
- **Colecciones concurrentes** (`ConcurrentHashMap.newKeySet()`) para detección de duplicados entre hilos.
- **`CustomSkipPolicy` centralizada**, listeners de monitoreo (`BatchJobListener`, `BatchStepListener`, `BatchSkipListener`), logging con SLF4J, y ajuste del pool de HikariCP (`maximum-pool-size=6`).

## Novedades Semana 3: escalado con particiones (`PartitionStep`)

Se reemplazó el paralelismo a nivel de *item* (Semana 2) por paralelismo a nivel de *step*, usando particiones de Spring Batch, e incorporando el dataset oficial de 1000 filas por CSV.

- **`LineRangePartitioner`**: reparte las líneas del CSV en rangos según `gridSize`.
- **Readers `@StepScope`**: cada partición lee únicamente su tramo del archivo.
- **Step "manager" + Step "worker"** por cada Job, ejecutados en paralelo por `batchTaskExecutor`.

### Comparación de parámetros: buscando el gridSize óptimo

| gridSize | Filas por partición | Duración total del Job |
|---|---|---|
| 2 | 500 / 500 | 556 ms |
| **3** | ~334 c/u | **366 ms** ⭐ |
| 4 | 250 c/u | 500 ms |

**`gridSize=3` resultó óptimo** porque coincide exactamente con `corePoolSize`/`maxPoolSize`=3 del `batchTaskExecutor`: con 2 el pool queda subutilizado, y con 4 la partición extra debe esperar un hilo libre, sumando overhead sin ganancia real. El valor final quedó en `application.properties` como `batch.partition.grid-size=3`.

---

## Novedades Exp2 Semana 4: Backend for Frontend (BFF)

### Objetivo de esta etapa

Exponer los datos ya cargados por los Jobs de Batch (transacciones, cuentas con interés, movimientos anuales) a través de **tres backends independientes**, uno por tipo de cliente: **Web**, **Móvil** y **Cajero Automático**, cada uno con su propia forma de autenticación, su propio conjunto de campos expuestos, y su propia organización de código.

### Estrategia de implementación elegida

Se evaluaron las 3 estrategias de BFF presentadas en la guía (backends independientes por cliente, diseño de endpoints personalizados, y aprovechar microservicios). Se eligió **"Diseño de endpoints personalizados"**: una sola aplicación Spring Boot expone rutas, DTOs y reglas de seguridad distintas por canal (`/api/web/**`, `/api/mobile/**`, `/api/atm/**`), en vez de desplegar tres aplicaciones separadas.

**Por qué esta estrategia y no las otras dos:**
- *Backends independientes por repositorio/deploy separado* habría triplicado la infraestructura (3 apps, 3 configuraciones de seguridad y base de datos) sin aportar beneficio real para el alcance de esta actividad.
- *Aprovechar microservicios* no aplica porque el proyecto es un monolito Spring Batch + JPA, no una arquitectura de microservicios.
- El enunciado pide avanzar "en la continuidad" del proyecto ya existente, lo que refuerza mantener una única aplicación desplegable.

El análisis completo, con la tabla comparativa de las 3 estrategias y la justificación detallada, está en [`Exp2_S4_Analisis_Estrategia_BFF.md`](./Exp2_S4_Analisis_Estrategia_BFF.md).

### Los 3 BFF implementados

| BFF | Endpoint(s) | Rol de seguridad | Qué expone |
|---|---|---|---|
| **Web** | `GET /api/web/cuentas/{id}`, `GET /api/web/transacciones` | `ROLE_WEB` | Datos completos: `cuentaId`, `nombre`, `edad`, `tipo`, `saldoInicial`, `saldoFinal`, historial completo de movimientos, transacciones con su `anomalia` |
| **Móvil** | `GET /api/mobile/cuentas/{id}` | `ROLE_MOBILE` | Datos esenciales: `cuentaId`, `nombre`, `saldoActual`, y solo los 5 movimientos más recientes |
| **Cajero (ATM)** | `GET /api/atm/cuentas/{id}/saldo`, `POST /api/atm/cuentas/{id}/retiro` | `ROLE_ATM` | Mínimo indispensable: `cuentaId` + `saldoDisponible`; el retiro valida saldo suficiente y devuelve `422` con mensaje claro si no lo hay |

### Autenticación y autorización por canal (histórico Exp2 S4, reemplazado por JWT en S5)

> Desde Exp2 S5 la autenticación es 100% por token JWT (ver sección más abajo). Se deja esta referencia solo con fines históricos/comparativos.

En la Semana 4 se usó **Spring Security con Basic Auth** y un usuario/rol distinto por canal (`SecurityConfig.java`):

| Canal | Usuario | Contraseña | Rol requerido |
|---|---|---|---|
| Web | `web-client` | `web-secret` | `ROLE_WEB` |
| Móvil | `mobile-client` | `mobile-secret` | `ROLE_MOBILE` |
| Cajero | `atm-client` | `atm-secret` | `ROLE_ATM` |

Un cliente autenticado para un canal **no puede** acceder a los endpoints de otro canal (probado: `mobile-client` contra `/api/web/**` responde `403 Forbidden`, y lo mismo en sentido inverso — ver evidencias de ejecución).

### Organización del código (paquete `bff/`)

```
bff/
├── dto/          # DTOs específicos por canal, sin reutilizar entre canales
├── web/          # WebBffController + WebBffService
├── mobile/       # MobileBffController + MobileBffService
├── atm/          # AtmBffController + AtmBffService
└── exception/    # SaldoInsuficienteException + BffExceptionHandler (manejo de errores común)
```

Los 3 BFF reutilizan las entidades y repositorios ya existentes del proyecto batch (`TransaccionRepository`, `CuentaInteresRepository`, `CuentaAnualRepository`), sumando un único método nuevo: `CuentaAnualRepository.findByCuentaId(Long)`.

### Cómo probar los BFF (histórico Exp2 S4, con Basic Auth)

> Desde Exp2 S5 la autenticación cambió a JWT y el servidor solo responde por HTTPS (puerto 8443). Ver la sección "Cómo probar los BFF (con JWT + HTTPS)" más abajo para los comandos actualizados.

```bash
# Web (datos completos)
curl -u web-client:web-secret http://localhost:8080/api/web/cuentas/124
```

---

## Novedades Exp2 Semana 5: HTTPS, tokens JWT y optimización de recursos

### 1) Estrategia de implementación de BFF (revisión Semana 5)

Se ratifica la estrategia **"Diseño de endpoints personalizados"** elegida en la Semana 4 (ver [`Exp2_S4_Analisis_Estrategia_BFF.md`](./Exp2_S4_Analisis_Estrategia_BFF.md)): una sola aplicación Spring Boot, tres conjuntos de rutas/DTOs/reglas de seguridad (`/api/web/**`, `/api/mobile/**`, `/api/atm/**`). Sigue siendo la mejor opción para un equipo pequeño que necesita extender — no reescribir — el monolito Batch ya existente.

### 2) HTTPS en los 3 BFF

Todo el tráfico se sirve exclusivamente por HTTPS:

- `server.ssl.enabled=true`, con un certificado autofirmado PKCS12 (`src/main/resources/keystore.p12`, alias `bancoxyz-bff`) cargado desde `application.properties`.
- La app escucha en **`https://localhost:8443`** para los 3 canales.
- `HttpsRedirectConfig` agrega un segundo conector Tomcat en el puerto 8080 que **redirige automáticamente** cualquier solicitud HTTP hacia HTTPS, de modo que ningún BFF queda accesible sin cifrar.
- El certificado se generó con:
  ```bash
  keytool -genkeypair -alias bancoxyz-bff -keyalg RSA -keysize 2048 -validity 3650 \
    -storetype PKCS12 -keystore src/main/resources/keystore.p12 -storepass bancoxyz123 \
    -dname "CN=localhost, OU=BancoXYZ, O=DuocUC, L=Santiago, ST=RM, C=CL"
  ```
  Por ser autofirmado, el navegador, `curl` o Postman mostrarán una advertencia de certificado no confiable (`curl -k` para pruebas locales, o desactivar "SSL certificate verification" en Postman Settings → General); en un despliegue real se reemplazaría por un certificado emitido por una CA.

### 3) Autenticación y autorización por canal con JWT

Se reemplazó Basic Auth (Semana 4) por **tokens JWT firmados (HS256)**:

1. Cada canal se autentica **una sola vez** contra `POST /api/auth/login` con sus credenciales (las mismas de Semana 4: `web-client/web-secret`, `mobile-client/mobile-secret`, `atm-client/atm-secret`).
2. Recibe un JWT con claims `canal` y `rol`, válido por 30 minutos (`bff.jwt.expiration-minutes`).
3. Reenvía ese token en el header `Authorization: Bearer <token>` en cada llamada a su BFF. `JwtAuthenticationFilter` lo valida en cada request (autenticación *stateless*, sin sesión de servidor) y `SecurityConfig` autoriza según el rol contra el path exacto del canal — un token del canal Móvil sigue sin poder usarse contra `/api/web/**` (403 Forbidden).

| Canal | Usuario | Contraseña | Rol / claim `rol` |
|---|---|---|---|
| Web | `web-client` | `web-secret` | `WEB` |
| Móvil | `mobile-client` | `mobile-secret` | `MOBILE` |
| Cajero | `atm-client` | `atm-secret` | `ATM` |

### 4) Optimización de respuestas y consumo de recursos por canal

- **Compresión gzip** habilitada a nivel de servidor (`server.compression.enabled=true`) para las 3 APIs.
- **Paginación** en `GET /api/web/transacciones` (`?page=0&size=20`), para no serializar el dataset completo en cada llamada del canal Web.
- **Cache-Control por canal**, según la sensibilidad y frecuencia de cambio de cada dato:
  - Web: `max-age=30` (datos de auditoría, cambian poco).
  - Móvil: `max-age=15` (saldo resumido, se tolera algo de latencia en la propagación a cambio de menos llamadas repetidas desde la app).
  - Cajero: `no-store` (saldo y retiro son operaciones críticas; nunca se sirven desde caché).

### Cómo probar los BFF (con JWT + HTTPS)

Con el proyecto corriendo (ver "Instrucciones para ejecutar" más abajo):

```bash
# 1) Login por canal: se obtiene el token JWT (curl -k por certificado autofirmado)
TOKEN_WEB=$(curl -sk -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"web-client","password":"web-secret"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

TOKEN_MOBILE=$(curl -sk -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mobile-client","password":"mobile-secret"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

TOKEN_ATM=$(curl -sk -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"atm-client","password":"atm-secret"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# 2) Web (datos completos, paginado)
curl -sk https://localhost:8443/api/web/cuentas/124 -H "Authorization: Bearer $TOKEN_WEB"
curl -sk "https://localhost:8443/api/web/transacciones?page=0&size=10" -H "Authorization: Bearer $TOKEN_WEB"

# 3) Móvil (datos livianos)
curl -sk https://localhost:8443/api/mobile/cuentas/124 -H "Authorization: Bearer $TOKEN_MOBILE"

# 4) Cajero: consulta de saldo y retiro
curl -sk https://localhost:8443/api/atm/cuentas/124/saldo -H "Authorization: Bearer $TOKEN_ATM"
curl -sk -X POST https://localhost:8443/api/atm/cuentas/124/retiro \
  -H "Authorization: Bearer $TOKEN_ATM" -H "Content-Type: application/json" \
  -d '{"monto": 2000}'

# 5) Aislamiento entre canales (debe dar 403 Forbidden): token Móvil contra el BFF Web
curl -sk -i https://localhost:8443/api/web/cuentas/124 -H "Authorization: Bearer $TOKEN_MOBILE"

# 6) Sin token (debe dar 401/403): confirma que ya no existe acceso anónimo
curl -sk -i https://localhost:8443/api/atm/cuentas/124/saldo

# 7) HTTP simple redirige a HTTPS (debe responder 3xx hacia https://localhost:8443/...)
curl -i http://localhost:8080/api/web/cuentas/124
```

También hay una **colección de Postman lista para importar** (`BancoXYZ_BFF.postman_collection.json` + `BancoXYZ_Local.postman_environment.json`), con los mismos casos organizados en carpetas `Auth`, `Web`, `Mobile`, `ATM` y `Seguridad (casos negativos)`, incluyendo scripts que guardan los tokens automáticamente tras el login.

Las capturas de cada una de estas pruebas (login, uso del token, aislamiento entre canales, redirección HTTPS) están en `Evidencias.docx`, incluida en esta entrega.

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.0** / **Spring Batch 6**
- **Spring Data JPA** (Hibernate)
- **Spring Web** (BFF REST — Exp2 S4)
- **Spring Security + JWT (jjwt 0.12.6)** — autenticación/autorización por canal, 100% stateless (Exp2 S5)
- **HTTPS / TLS** con certificado autofirmado PKCS12, redirección automática desde HTTP (Exp2 S5)
- **PostgreSQL 16** (vía contenedor Docker)
- **Maven**

## Instrucciones para ejecutar el proyecto

### 1. Levantar la base de datos (PostgreSQL vía Docker)

```bash
docker run --name banco-xyz-postgres -e POSTGRES_DB=bancoxyz -e POSTGRES_USER=bancoxyz -e POSTGRES_PASSWORD=bancoxyz123 -p 5432:5432 -d postgres:16
```

Si el contenedor ya existe de una ejecución anterior, solo necesitas iniciarlo:

```bash
docker start banco-xyz-postgres
```

Verifica que quedó corriendo:

```bash
docker ps
```

### 2. Ejecutar la aplicación

Desde la raíz del proyecto:

```bash
./mvnw clean spring-boot:run
```

Se recomienda usar `clean` para evitar errores por clases compiladas de una versión anterior del código (`target/` desactualizado).

> Durante la compilación aparecen `[WARNING]` de deprecación (`JobLauncher`, `chunk(int, PlatformTransactionManager)`, `taskExecutor(...)`). Son advertencias esperadas de Spring Batch 6.x y no afectan la compilación ni la ejecución del proyecto.

Al iniciar, la aplicación:
1. Crea automáticamente las tablas (`transacciones_procesadas`, `cuentas_interes`, `cuentas_anuales`) en PostgreSQL.
2. Ejecuta los 3 Jobs de Batch en secuencia: `transaccionJob` → `cuentaInteresJob` → `cuentaAnualJob`.
3. Levanta el servidor REST por **HTTPS en el puerto `8443`** (Tomcat) con los 3 BFF disponibles, más un conector HTTP en el puerto `8080` que solo redirige hacia HTTPS (ver "Novedades Exp2 Semana 5" más abajo).

### 3. Verificar los datos persistidos

```bash
docker exec -it banco-xyz-postgres psql -U bancoxyz -d bancoxyz -c "SELECT cuenta_id, nombre, saldo_final FROM cuentas_interes LIMIT 5;"
```

### 4. Probar los BFF

Ver la sección "Cómo probar los BFF (con JWT + HTTPS)" más arriba, o importar directamente la colección de Postman incluida en esta entrega.

## Datos de origen

Los archivos CSV (`src/main/resources/data/`) provienen de [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data) y simulan un sistema legacy con problemas de calidad de datos intencionales, resueltos por los `ItemProcessor` de este proyecto. Se usa el dataset oficial (1000 filas por archivo, carpeta `semana_3` del repo de origen).