# Banco XYZ - Migración de Procesos Batch con Spring Batch

## Objetivo del proyecto

Este proyecto moderniza tres procesos batch legacy del **Banco XYZ** (un banco ficticio) utilizando **Spring Batch**. A partir de datos legacy en formato CSV —con problemas típicos de sistemas antiguos como montos negativos, fechas mal formateadas, registros duplicados y campos vacíos—, se implementan tres Jobs que leen, validan/transforman y persisten la información en una base de datos relacional (PostgreSQL).

## Procesos implementados

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
│   └── CuentaAnualJobConfig.java   # Job + Step 3
├── entity/            # Entidades JPA (tablas destino ya validadas)
├── model/              # POJOs de lectura cruda del CSV
└── repository/          # Repositorios Spring Data JPA
```

Cada Job sigue el patrón estándar de Spring Batch: **ItemReader** (lee el CSV) → **ItemProcessor** (valida, transforma y detecta anomalías) → **ItemWriter** (persiste en PostgreSQL).

> **Nota de implementación:** los beans `*Reader` exponen un `SynchronizedItemStreamReader<X>` (necesario para lectura thread-safe con 3 hilos). Los `*JobConfig` inyectan ese reader directamente como `ItemStreamReader<X>` y lo usan tal cual en `.reader(...)`, sin volver a envolverlo — evita el error `No qualifying bean of type 'FlatFileItemReader'` que aparece si se inyecta el tipo equivocado o se envuelve el reader dos veces.

## Manejo de errores

Cada `Step` está configurado con `faultTolerant()`:
- **Skip**: los registros con datos irrecuperables (ej. fechas con formato irreconocible) se omiten sin detener el Job completo, hasta un límite configurable por Step/partición (`batch.skip-limit`, 300 por defecto — ver Semana 3), gestionado mediante una `CustomSkipPolicy` (ver detalle abajo).
- **Retry**: ante fallos transitorios de conexión a la base de datos, se reintenta hasta 3 veces (`retryLimit`) con una espera creciente entre intentos (`ExponentialBackOffPolicy`) antes de fallar.
- **SkipListener**: cada registro omitido queda registrado en el log con el motivo.

Además, cada `ItemProcessor` detecta y reporta (vía logger) anomalías de calidad de datos que no impiden el guardado (duplicados, edades fuera de rango, etc.), simulando las validaciones que exige un proceso de migración de un sistema legacy. Dos de esas validaciones sí se consideran datos irrecuperables y disparan un skip real (`DatoInvalidoException`), en vez de solo loguear la anomalía:

- `CuentaInteresProcessor`: un `tipo` de cuenta que no sea `ahorro`, `prestamo` o `hipoteca` (ej. `-1`) no tiene una tasa de interés definida, por lo que el registro se omite en vez de guardarse con tasa `0.0`.
- `CuentaAnualProcessor`: un `monto` nulo o en cero no representa un movimiento real de cuenta, por lo que el registro se omite en vez de guardarse como un movimiento vacío.

En ambos casos, la `CustomSkipPolicy` clasifica la excepción como omisible, el `BatchSkipListener` deja constancia del motivo en el log, y el Job continúa hasta completarse (`COMPLETED`) sin detenerse por estos registros.

## Novedades Semana 2: procesamiento paralelo y optimización de recursos

Esta semana se optimizó la ejecución de los 3 Jobs incorporando procesamiento paralelo, monitoreo y un manejo de errores más robusto, manteniendo el mismo modelo de datos y las mismas validaciones funcionales de la Semana 1:

- **Procesamiento multihilo (3 hilos por Step)**: se agregó `BatchAsyncConfig`, que define un `TaskExecutor` (`ThreadPoolTaskExecutor`) con `corePoolSize`/`maxPoolSize` = 3 y cola de espera de 25 elementos. Este executor se inyecta en el `taskExecutor()` de cada `Step` (`transaccionStep`, `cuentaInteresStep`, `cuentaAnualStep`) para procesar los chunks en paralelo.
- **Lectura thread-safe**: como los 3 `Reader` (`FlatFileItemReader`) ahora se comparten entre hilos, cada uno se envuelve con `SynchronizedItemStreamReader` (vía `SynchronizedItemStreamReaderBuilder`) para evitar condiciones de carrera al leer el CSV.
- **Colecciones concurrentes en los `Processor`**: las estructuras usadas para detectar duplicados (`clavesVistas` en `TransaccionProcessor` y `CuentaInteresProcessor`) pasaron de `HashSet` a `ConcurrentHashMap.newKeySet()`, ya que ahora reciben escrituras simultáneas desde varios hilos.
- **`CustomSkipPolicy` centralizada**: reemplaza los `.skip(Clase.class)` sueltos de cada Step. Solo omite errores esperables de calidad de datos (`DatoInvalidoException`, `FlatFileParseException`); cualquier otro error (ej. de infraestructura) detiene el Step en vez de omitirse a ciegas.
- **Listeners de monitoreo** (nuevos, en `batch/`):
  - `BatchJobListener`: registra inicio/fin de cada Job y su duración total.
  - `BatchStepListener`: registra inicio/fin de cada Step, hilo de ejecución, cantidad de registros leídos/escritos/omitidos y duración.
  - `BatchSkipListener`: registra en el log cada omisión (lectura, procesamiento o escritura) y su causa.
- **Migración de `System.out.println` a logging con SLF4J** en `BatchRunner` y en los `ItemProcessor`, incluyendo niveles configurados en `application.properties` (`logging.level.com.duoc.bancoxyzbatch=INFO`, `logging.level.org.springframework.batch=INFO`).
- **Ajuste del pool de conexiones (HikariCP)** en `application.properties` para acompañar el paralelismo: `maximum-pool-size=6`, `minimum-idle=3`, `connection-timeout=30000` (3 hilos batch + margen de conexiones auxiliares).
- **Mejoras de validación en los `Processor`**: se agrega `trim()` a campos de texto (fecha, tipo, descripción) antes de validarlos y se valida explícitamente que el campo `tipo` de las transacciones sea `debito` o `credito`.
- **Datos de prueba ampliados**: se agregaron nuevos casos a los CSV de origen (fechas con formato legacy `yyyy/MM/dd`, montos y edades vacíos, tipos inválidos como `invalid` o `-1`, edad límite 100, descripción en blanco) para ejercitar los nuevos mecanismos de skip y las validaciones reforzadas.

## Novedades Semana 3: escalado con particiones (`PartitionStep`)

Esta semana se reemplazó el paralelismo a nivel de *item* (multi-thread dentro de un mismo Step, Semana 2) por paralelismo a nivel de *step*, usando **particiones de Spring Batch**. Además, se incorporó el dataset oficial de la Semana 3 (1000 filas por CSV, con una proporción alta de datos inválidos a propósito), en reemplazo del CSV de prueba reducido de las Semanas 1-2.

### Cómo funciona

- **`LineRangePartitioner`** (`batch/partition/`): cuenta las líneas de datos del CSV (sin el header) y las reparte en rangos según `gridSize`. Cada partición recibe un `ExecutionContext` con `startLine` y `linesToRead`.
- **Readers step-scoped**: los tres `*Reader` (`TransaccionReader`, `CuentaInteresReader`, `CuentaAnualReader`) pasaron a ser `@StepScope`, inyectando `startLine`/`linesToRead` vía *late binding* (`@Value("#{stepExecutionContext['...']}")`) para que cada partición lea únicamente el tramo del archivo que le corresponde.
- **Step "manager" + Step "worker"**: cada Job ahora tiene un `...PartitionStep` (manager) que reparte el `...WorkerStep` (worker, el chunk de siempre: reader → processor → writer, con `faultTolerant`, skip y retry) en N particiones ejecutadas en paralelo por `batchTaskExecutor`.
- **`batch.partition.grid-size`** (`application.properties`): cantidad de particiones por Job, configurable sin tocar código.
- **`batch.skip-limit`**: el límite de omisiones por Step/partición, que subió de 10 (fijo, calibrado para el CSV de prueba de 9 filas) a un valor configurable (300 por defecto), porque el dataset oficial de 1000 filas trae muchos más registros inválidos a propósito y cada partición tiene su propio contador de skips independiente.

### Comparación de parámetros: buscando el gridSize óptimo

Se ejecutó `cuentaAnualJob` (1000 filas) tres veces, cambiando solo `batch.partition.grid-size`, y se midió la duración total del Job reportada por `BatchJobListener`:

| gridSize | Filas por partición | Duración por partición | Duración total del Job |
|---|---|---|---|
| 2 | 500 / 500 | 519 ms, 554 ms | **556 ms** |
| **3** | ~334 c/u | 350 ms, 364 ms, ~267 ms | **366 ms** ⭐ |
| 4 | 250 c/u | 259 ms, 260 ms, 283 ms, 237 ms | **500 ms** |

**`gridSize=3` resultó la configuración óptima**, y no por casualidad: `batchTaskExecutor` (definido en la Semana 2, `BatchAsyncConfig`) tiene `corePoolSize`/`maxPoolSize` = **3**, es decir, solo 3 hilos disponibles para ejecutar particiones en paralelo.

- Con `gridSize=2` el pool queda subutilizado (2 de 3 hilos activos) y cada partición carga el doble de filas, aumentando el tiempo por partición.
- Con `gridSize=3` las 3 particiones corren simultáneamente, una por hilo, logrando el máximo paralelismo real del pool configurado.
- Con `gridSize=4` la 4ª partición debe esperar a que se libere un hilo (solo hay 3 en el pool), sumando latencia de cola y overhead de coordinación extra sin ninguna ganancia de velocidad.

**Conclusión:** el número óptimo de particiones no es "cuantas más, mejor", sino que debe igualar la capacidad real del `TaskExecutor` subyacente. Por eso `application.properties` quedó con `batch.partition.grid-size=3` como valor final.



- **Java 21**
- **Spring Boot 4.1.0** / **Spring Batch 6**
- **Spring Data JPA** (Hibernate)
- **PostgreSQL 16** (vía contenedor Docker)
- **Maven**

## Instrucciones para ejecutar el proyecto

### 1. Levantar la base de datos (PostgreSQL vía Docker)

```bash
docker run --name banco-xyz-postgres -e POSTGRES_DB=bancoxyz -e POSTGRES_USER=bancoxyz -e POSTGRES_PASSWORD=bancoxyz123 -p 5432:5432 -d postgres:16
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

> Durante la compilación aparecen `[WARNING]` de deprecación (`JobLauncher`, `chunk(int, PlatformTransactionManager)`, `taskExecutor(...)`). Son advertencias esperadas de Spring Batch 6.x (que reemplaza esas APIs por `JobOperator` y una nueva sintaxis de `StepBuilder`) y no afectan la compilación ni la ejecución del proyecto.

Al iniciar, la aplicación:
1. Crea automáticamente las tablas (`transacciones_procesadas`, `cuentas_interes`, `cuentas_anuales`) en PostgreSQL.
2. Ejecuta los 3 Jobs en secuencia: `transaccionJob` → `cuentaInteresJob` → `cuentaAnualJob`.
3. Imprime en consola el detalle de cada anomalía detectada y el resultado (`COMPLETED`) de cada Job.

### 3. Verificar los datos persistidos

Puedes conectarte a la base con cualquier cliente PostgreSQL (DBeaver, pgAdmin, `psql`, etc.):

- Host: `localhost`
- Puerto: `5432`
- Base de datos: `bancoxyz`
- Usuario: `bancoxyz`
- Contraseña: `bancoxyz123`

Si no tienes el cliente `psql` instalado localmente, puedes usar el que ya viene incluido en el contenedor de Postgres:

```bash
docker exec -it banco-xyz-postgres psql -U bancoxyz -d bancoxyz
```

Y luego, dentro de la sesión de `psql`:

```sql
SELECT * FROM transacciones_procesadas;
SELECT * FROM cuentas_interes;
SELECT * FROM cuentas_anuales;
```

## Datos de origen

Los archivos CSV (`src/main/resources/data/`) provienen de [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data) y simulan un sistema legacy con problemas de calidad de datos intencionales, resueltos por los `ItemProcessor` de este proyecto. Desde la Semana 3 se usa el dataset oficial (1000 filas por archivo), en reemplazo del CSV de prueba reducido (9 filas) de las Semanas 1-2.