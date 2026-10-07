# Instrucciones de ejecución y prueba

Guía para levantar, probar y escalar la solución del Banco XYZ. Todo se hace con Docker; no necesitas instalar Java ni Maven.

## 1. Requisitos

- Docker Desktop (o Docker Engine) con **Compose v2** (`docker compose version`).
- **8 GB de RAM** libres para Docker (hay ~16 contenedores; las JVM están acotadas con `-XX:MaxRAMPercentage=60 -XX:+UseSerialGC`).
- `curl` (Git Bash o WSL en Windows). Opcional: `jq`.
- Puertos libres: `8443`, `9000`, `8761`, `8888`, `29092`, `5432`, `5435-5437`.

## 2. Levantar todo

```bash
git clone https://github.com/Francisca-Valenzuela/Exp1_S1_Grupo5.git
cd Exp1_S1_Grupo5

docker compose up -d --build       # primera vez: 5-10 min (compila 11 imágenes)
docker compose ps                   # esperar a que todo esté (healthy)
```

> Si ya habías levantado una versión anterior del proyecto: `docker compose down -v` antes de empezar
> (cambió el esquema de bases de datos y se eliminó el broker JMS/Artemis).

Orden de arranque automático: bases de datos + Kafka + Eureka → Config Server → Auth Server → **core (ejecuta los 3 jobs batch)**
→ microservicios → BFFs → API Gateway.

**Paneles útiles**

| URL | Qué ver |
|---|---|
| http://localhost:8761 | Eureka: debe listar `CUENTAS-SERVICE` (2), `PAGOS-SERVICE` (2), `CLIENTES-SERVICE`, `BFF-*`, `BANCO-XYZ-CORE`, `API-GATEWAY` |
| http://localhost:8888/cuentas-service/default | Configuración que entrega Config Server |
| https://localhost:8443 | API Gateway (certificado autofirmado: usar `curl -k`) |

## 3. Verificar los procesos batch

```bash
docker compose logs banco-xyz-core | grep -E "Job [123]|Resumen|FALLO|publicados"
```

Debe verse `Resumen -> transaccionJob: COMPLETED, cuentaInteresJob: COMPLETED, cuentaAnualJob: COMPLETED` y la línea
`Eventos 'cuentas.migradas' publicados: 50`. Las filas descartadas por datos inválidos aparecen como `SKIP` en el log (por diseño).

Comprobar que la migración llegó a los microservicios (vía Kafka):

```bash
docker compose exec cuentas-db  psql -U cuentas  -d cuentas_db  -c "select count(*), sum(saldo) from cuentas;"
docker compose exec clientes-db psql -U clientes -d clientes_db -c "select count(*) from clientes;"
```
Ambos deben mostrar 50 registros (cuentas 101–150).

## 4. Obtener tokens (OAuth2.0, `client_credentials`)

Cada canal tiene su propio cliente y *scope*. Usa los secretos que definiste en `auth-server` para `web-client`, `mobile-client` y `atm-client`.
El cliente `internal-client` (secreto `internal-2026`) sirve para llamar directamente a los microservicios.

```bash
token() { curl -s -u "$1:$2" -d grant_type=client_credentials http://localhost:9000/oauth2/token | jq -r .access_token; }

WEB=$(token web-client       "$WEB_SECRET")
MOB=$(token mobile-client    "$MOBILE_SECRET")
ATM=$(token atm-client       "$ATM_SECRET")
INT=$(token internal-client  internal-2026)
```

## 5. Probar los BFF (un canal = una respuesta distinta)

```bash
G=https://localhost:8443

# Web: respuesta COMPLETA (cuenta + perfil de cliente + historial) -> 3 servicios agregados
curl -sk -H "Authorization: Bearer $WEB" $G/api/web/cuentas/101 | jq

# Móvil: respuesta LIGERA (saldo + 5 movimientos recientes)
curl -sk -H "Authorization: Bearer $MOB" $G/api/mobile/cuentas/101 | jq

# Cajero: solo cuentaId + saldo
curl -sk -H "Authorization: Bearer $ATM" $G/api/atm/cuentas/101/saldo | jq
```

**Autorización por canal** (cada token solo sirve en su canal):

```bash
curl -sk -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $MOB" $G/api/web/cuentas/101   # 403
curl -sk -o /dev/null -w "%{http_code}\n"                                  $G/api/web/cuentas/101   # 401
```

## 6. Retiro en cajero (Kafka asíncrono)

```bash
R=$(curl -sk -X POST -H "Authorization: Bearer $ATM" -H "Content-Type: application/json" \
     -d '{"monto": 1000}' $G/api/atm/cuentas/101/retiro); echo $R | jq
ID=$(echo $R | jq -r .solicitudId)

curl -sk -H "Authorization: Bearer $ATM" $G/api/atm/retiros/$ID | jq        # PENDIENTE -> PROCESADO
curl -sk -H "Authorization: Bearer $ATM" $G/api/atm/cuentas/101/saldo | jq  # saldo descontado
```
Reglas del cajero: monto ≤ 500.000 y múltiplo de 1.000 (si no, `400`). Un saldo insuficiente termina en estado `RECHAZADO` con el motivo.

## 7. Gestión de cuentas, clientes y pagos (microservicios)

```bash
# --- Clientes ---
curl -sk -X POST -H "Authorization: Bearer $INT" -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Pérez","edad":30,"email":"ana@mail.cl","telefono":"+56911112222"}' $G/api/clientes | jq   # -> clienteId 100000+
curl -sk -H "Authorization: Bearer $INT" "$G/api/clientes?page=0&size=5" | jq '.content | length'

# --- Cuentas: apertura, consulta, mantenimiento, cierre ---
curl -sk -X POST -H "Authorization: Bearer $INT" -H "Content-Type: application/json" \
  -d '{"clienteId":100000,"tipo":"ahorro","saldoInicial":50000}' $G/api/cuentas | jq    # -> cuentaId 100000+
curl -sk -H "Authorization: Bearer $INT" $G/api/cuentas/100000 | jq
curl -sk -X PUT  -H "Authorization: Bearer $INT" -H "Content-Type: application/json" -d '{"tipo":"corriente"}' $G/api/cuentas/100000 | jq
curl -sk -X POST -H "Authorization: Bearer $INT" $G/api/cuentas/100000/cierre | jq     # 422 si el saldo != 0

# --- Pagos: transferencia (asíncrona, 202 Accepted) ---
P=$(curl -sk -X POST -H "Authorization: Bearer $INT" -H "Content-Type: application/json" \
    -H "Idempotency-Key: demo-001" \
    -d '{"cuentaOrigenId":102,"cuentaDestinoId":103,"monto":2000,"descripcion":"demo"}' $G/api/pagos/transferencias)
echo $P | jq
curl -sk -H "Authorization: Bearer $INT" $G/api/pagos/$(echo $P | jq -r .solicitudId) | jq   # PENDIENTE -> COMPLETADO

# Repetir con el MISMO Idempotency-Key devuelve el mismo pago (no duplica)
# Depósito y pago:  POST /api/pagos/depositos  y  /api/pagos/pagos
```

**Eventos de seguridad (`alertas.seguridad`):** una transferencia ≥ 1.000.000 o una rechazada genera una alerta que sube el nivel de riesgo del cliente:

```bash
curl -sk -X POST -H "Authorization: Bearer $INT" -H "Content-Type: application/json" \
  -d '{"cuentaOrigenId":104,"cuentaDestinoId":105,"monto":99999999}' $G/api/pagos/transferencias | jq   # RECHAZADO (saldo)
curl -sk -H "Authorization: Bearer $INT" $G/api/clientes/104 | jq '{alertasSeguridad, nivelRiesgo, operacionesCompletadas}'
```

## 8. Ver los eventos en Kafka

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic transacciones.completadas --from-beginning --max-messages 3

docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic alertas.seguridad --from-beginning --max-messages 3
```

## 9. Escalabilidad horizontal y balanceo

```bash
docker compose ps cuentas-service pagos-service                 # 2 réplicas cada uno
docker compose up -d --scale cuentas-service=3 --scale bff-web=2 --no-recreate
# Eureka (http://localhost:8761) mostrará las nuevas instancias; el gateway las incluye en el round-robin.

# Ver el balanceo: cada réplica responde (los logs se reparten)
for i in $(seq 1 10); do curl -sk -o /dev/null -H "Authorization: Bearer $INT" $G/api/cuentas/101; done
docker compose logs --tail=20 cuentas-service
```
Kafka reparte las 3 particiones de cada tópico entre las réplicas del mismo servicio (grupo de consumo).

## 10. Pruebas de resiliencia (tolerancia a fallos)

| Prueba | Comando | Resultado esperado |
|---|---|---|
| **Cae clientes-service** | `docker compose stop clientes-service` y consultar `/api/web/cuentas/101` | `200` con `"datosParciales": true` (sin perfil de cliente) |
| **Cae cuentas-service** | `docker compose stop cuentas-service` y consultar `/api/mobile/cuentas/101` | `503` con mensaje claro (no un 500 ni un cuelgue) |
| **Circuit Breaker** | tras varias fallas, `docker compose exec bff-web wget -qO- http://127.0.0.1:8081/actuator/circuitbreakers` | estado `OPEN` → luego `HALF_OPEN` → `CLOSED` al recuperarse |
| **Cae Kafka** | `docker compose stop kafka`, crear una transferencia | `202` con estado `PENDIENTE` (queda en el *outbox*); al hacer `docker compose start kafka` se completa sola |
| **Reinicio de un servicio** | `docker compose restart cuentas-service` durante un retiro | el retiro se aplica **una sola vez** (idempotencia) |
| **Reejecución batch** | `docker compose stop kafka && docker compose restart banco-xyz-core` | el job de intereses falla en su último step y se reintenta (log `FALLO ... Se reiniciara desde el step fallido`); al volver Kafka termina `COMPLETED` |

Recuperar el entorno: `docker compose start kafka clientes-service cuentas-service`.

## 11. Monitoreo

```bash
docker compose exec cuentas-service wget -qO- http://127.0.0.1:8085/actuator/health
docker compose exec cuentas-service wget -qO- http://127.0.0.1:8085/actuator/circuitbreakers
docker compose --profile monitoring up -d prometheus      # http://localhost:9090 (targets por DNS de Docker)
```

## 12. Ejecución local sin Docker (desarrollo)

Requiere Java 21 y Maven. Levanta solo la infraestructura con Docker y los servicios con Maven, **en este orden**:

```bash
docker compose up -d kafka banco-xyz-postgres cuentas-db clientes-db pagos-db   # puertos 29092 / 5432 / 5435-5437
(cd eureka-server  && ./mvnw spring-boot:run) &
(cd config-server  && ./mvnw spring-boot:run) &
(cd auth-server    && ./mvnw spring-boot:run) &
(cd banco-xyz-core && ./mvnw spring-boot:run) &
(cd cuentas-service && ./mvnw spring-boot:run) &  (cd clientes-service && ./mvnw spring-boot:run) &  (cd pagos-service && ./mvnw spring-boot:run) &
(cd bff-web && ./mvnw spring-boot:run) &  (cd bff-mobile && ./mvnw spring-boot:run) &  (cd bff-atm && ./mvnw spring-boot:run) &
(cd api-gateway && ./mvnw spring-boot:run) &
```
Sin el perfil `docker`, los servicios usan `localhost` (Kafka `localhost:29092`, Eureka `localhost:8761`, issuer `http://localhost:9000`).

## 13. Apagar

```bash
docker compose down        # conserva los datos
docker compose down -v     # borra también las bases de datos (migración desde cero)
```

## 14. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `401` con un token recién emitido | El `iss` del token debe coincidir con `issuer-uri`. En Docker ambos son `http://auth-server:9000` (`AUTH_ISSUER` en el compose y `config-repo/application-docker.yml`). El token se puede pedir a `http://localhost:9000`. |
| Contenedores reiniciándose por memoria | Subir la memoria de Docker a 8 GB o apagar réplicas: `--scale cuentas-service=1 --scale pagos-service=1`. |
| `cuentas`/`clientes` vacíos | Kafka no estaba listo cuando corrió el job: `docker compose restart banco-xyz-core`. |
| `curl: (60) SSL certificate` | Certificado autofirmado: usar `curl -k` (o importar `api-gateway/src/main/resources/keystore.p12`). |
| Una réplica de `bff-atm` responde 404 al consultar un retiro | El estado es en memoria; reintentar (el resultado llega por Kafka a todas las réplicas) — ver limitaciones en `readme.md`. |
