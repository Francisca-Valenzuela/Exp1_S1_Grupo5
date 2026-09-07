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
│   └── SecurityConfig.java         # Autenticación/autorización por canal BFF (Exp2 S4)
├── entity/            # Entidades JPA (tablas destino ya validadas)
├── model/              # POJOs de lectura cruda del CSV
├── repository/          # Repositorios Spring Data JPA
└── bff/                 # Backend for Frontend (Exp2 S4) — ver detalle más abajo
    ├── dto/
    ├── web/
    ├── mobile/
    ├── atm/
    └── exception/
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

### Autenticación y autorización por canal

Se usa **Spring Security con Basic Auth** y un usuario/rol distinto por canal (`SecurityConfig.java`):

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

### Cómo probar los BFF

Con el proyecto corriendo (ver "Instrucciones para ejecutar" más abajo), usa Postman o `curl` con Basic Auth:

```bash
# Web (datos completos)
curl -u web-client:web-secret http://localhost:8080/api/web/cuentas/124

# Móvil (datos livianos)
curl -u mobile-client:mobile-secret http://localhost:8080/api/mobile/cuentas/124

# Cajero: consulta de saldo
curl -u atm-client:atm-secret http://localhost:8080/api/atm/cuentas/124/saldo

# Cajero: retiro (Postman recomendado por temas de escapado de comillas en PowerShell)
# POST /api/atm/cuentas/124/retiro
# Body raw JSON: {"monto": 2000}

# Aislamiento entre canales (debe dar 403)
curl -i -u mobile-client:mobile-secret http://localhost:8080/api/web/cuentas/124
```

Las capturas de cada una de estas pruebas están en `Evidencias.docx`, incluida en esta entrega.

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.1.0** / **Spring Batch 6**
- **Spring Data JPA** (Hibernate)
- **Spring Web** (BFF REST — Exp2 S4)
- **Spring Security** (Basic Auth por canal — Exp2 S4)
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
3. Levanta el servidor REST (Tomcat, puerto `8080`) con los 3 BFF disponibles.

### 3. Verificar los datos persistidos

```bash
docker exec -it banco-xyz-postgres psql -U bancoxyz -d bancoxyz -c "SELECT cuenta_id, nombre, saldo_final FROM cuentas_interes LIMIT 5;"
```

### 4. Probar los BFF

Ver la sección "Cómo probar los BFF" más arriba, o el detalle paso a paso en `Exp2_S4_Guia_Pruebas_Postman.md`.

## Datos de origen

Los archivos CSV (`src/main/resources/data/`) provienen de [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data) y simulan un sistema legacy con problemas de calidad de datos intencionales, resueltos por los `ItemProcessor` de este proyecto. Se usa el dataset oficial (1000 filas por archivo, carpeta `semana_3` del repo de origen).