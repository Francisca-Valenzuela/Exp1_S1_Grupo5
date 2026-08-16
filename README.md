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

## Manejo de errores

Cada `Step` está configurado con `faultTolerant()`:
- **Skip**: los registros con datos irrecuperables (ej. fechas con formato irreconocible) se omiten sin detener el Job completo, hasta un límite de 10 por ejecución (`skipLimit`).
- **Retry**: ante fallos transitorios de conexión a la base de datos, se reintenta hasta 3 veces (`retryLimit`) antes de fallar.
- **SkipListener**: cada registro omitido queda registrado en consola con el motivo.

Además, cada `ItemProcessor` detecta y reporta en consola anomalías de calidad de datos que no impiden el guardado (montos negativos/cero, duplicados, edades fuera de rango, descripciones faltantes), simulando las validaciones que exige un proceso de migración de un sistema legacy.

## Tecnologías

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
./mvnw spring-boot:run
```

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

## Datos de origen

Los archivos CSV (`src/main/resources/data/`) provienen de [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data) y simulan un sistema legacy con problemas de calidad de datos intencionales, resueltos por los `ItemProcessor` de este proyecto.