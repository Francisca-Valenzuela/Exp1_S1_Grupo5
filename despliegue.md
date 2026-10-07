# Despliegue en AWS

Arquitectura objetivo en AWS para la solución del Banco XYZ. Cada contenedor del `docker-compose.yaml` se convierte en un servicio
administrado, sin cambiar el código: solo cambian las **variables de entorno** (perfil `docker`/`aws`, URLs de Kafka y bases de datos).

## 1. Mapa Docker Compose → AWS

| Docker Compose | AWS | Notas |
|---|---|---|
| Imágenes `banco-xyz/*` | **Amazon ECR** (un repositorio por servicio) | `docker build` + `docker push` |
| Servicios Spring (cuentas, pagos, clientes, BFF, gateway…) | **Amazon ECS en Fargate** (1 *service* por microservicio) | Auto Scaling por CPU/latencia |
| `deploy.replicas` | `desiredCount` + **Application Auto Scaling** | Escalabilidad horizontal |
| `kafka` | **Amazon MSK** (Kafka administrado, 3 brokers, TLS) | `spring.kafka.bootstrap-servers` = endpoint MSK |
| Postgres ×4 | **Amazon RDS for PostgreSQL** (una instancia Multi-AZ con 4 bases, o una por servicio) | *database-per-service* |
| `api-gateway` + HTTPS | **Application Load Balancer** + certificado **ACM** | TLS terminado en el ALB |
| Eureka / Config Server | Se mantienen como servicios ECS (o **AWS Cloud Map** + **AWS AppConfig/Secrets Manager**) | |
| Variables sensibles | **AWS Secrets Manager** / SSM Parameter Store | contraseñas BD, secretos OAuth |
| Logs / métricas | **CloudWatch Logs** y **Container Insights** (+ Prometheus gestionado opcional) | |
| Red `banco-xyz-net` | **VPC** privada (subredes privadas para servicios/BD/MSK) | solo el ALB es público |

## 2. Pasos

### 2.1 Preparar la cuenta
```bash
aws configure                                   # credenciales y región (ej. us-east-1)
export AWS_ACCOUNT=<ID_CUENTA> AWS_REGION=us-east-1
export ECR=$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com
aws ecr get-login-password | docker login --username AWS --password-stdin $ECR
```

### 2.2 Publicar las imágenes en ECR
```bash
for s in eureka-server config-server auth-server banco-xyz-core cuentas-service clientes-service pagos-service bff-web bff-mobile bff-atm api-gateway; do
  aws ecr create-repository --repository-name banco-xyz/$s 2>/dev/null
  docker build -t $ECR/banco-xyz/$s:2.0 ./$s
  docker push $ECR/banco-xyz/$s:2.0
done
```

### 2.3 Red y datos
1. **VPC** con 2 AZ: subredes públicas (ALB) y privadas (ECS, RDS, MSK).
2. **RDS PostgreSQL 16** Multi-AZ en subred privada; crear `bancoxyz`, `cuentas_db`, `clientes_db`, `pagos_db` y un usuario por base. Guardar credenciales en **Secrets Manager**.
3. **Amazon MSK** (3 brokers, TLS). Crear los tópicos con 3 particiones y `replication.factor=3` (o dejar que `KafkaAdmin` los cree):
   `cuentas.migradas`, `pagos.solicitados`, `pagos.resultado`, `retiros.solicitados`, `retiros.resultado`, `transacciones.completadas`, `alertas.seguridad`.
4. **Security Groups:** ALB → gateway (8443); servicios → servicios (puertos 808x); servicios → RDS (5432); servicios → MSK (9094 TLS).

### 2.3.1 Variables de entorno por servicio (task definition)

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `docker` (o un perfil `aws` con `config-repo/application-aws.yml`) |
| `SPRING_CONFIG_IMPORT` | `configserver:http://config-server.banco.local:8888` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | endpoint RDS + secreto (`valueFrom` Secrets Manager) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | endpoints MSK (puerto 9094) |
| `SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL` | `SSL` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka-server.banco.local:8761/eureka/` |
| `AUTH_ISSUER` (auth-server) | URL pública/privada estable del auth-server |

> Los nombres `*.banco.local` provienen de un **namespace de AWS Cloud Map** (service discovery de ECS), análogo a la red de Docker Compose.

### 2.4 Servicios ECS (Fargate)
Crear un *cluster* `banco-xyz` y un *service* por imagen, en este orden:
1. `eureka-server` → 2. `config-server` → 3. `auth-server` → 4. `banco-xyz-core` (**desiredCount = 1**) →
5. `cuentas-service` (2), `clientes-service` (2), `pagos-service` (2) → 6. `bff-web`, `bff-mobile`, `bff-atm` (2) → 7. `api-gateway` (2, detrás del ALB).

Tamaños orientativos: 0,5 vCPU / 1 GB por microservicio; core 1 vCPU / 2 GB. Health check de contenedor: `wget -qO /dev/null http://127.0.0.1:<puerto>/actuator/health`.

### 2.5 Balanceador y HTTPS
1. **ALB** público con *listener* 443, certificado de **ACM** y *target group* → servicio `api-gateway` (puerto 8443, protocolo HTTPS; health check por HTTP al puerto de gestión 8090, ruta `/actuator/health`).
2. Registrar un dominio en **Route 53** apuntando al ALB.

### 2.6 Escalado automático
```bash
aws application-autoscaling register-scalable-target --service-namespace ecs \
  --resource-id service/banco-xyz/cuentas-service --scalable-dimension ecs:service:DesiredCount --min-capacity 2 --max-capacity 6
aws application-autoscaling put-scaling-policy --service-namespace ecs --policy-name cpu70 \
  --resource-id service/banco-xyz/cuentas-service --scalable-dimension ecs:service:DesiredCount \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{"TargetValue":70,"PredefinedMetricSpecification":{"PredefinedMetricType":"ECSServiceAverageCPUUtilization"}}'
```
El máximo útil de réplicas por servicio que consumen Kafka es el número de **particiones** (3); para más paralelismo, aumentar particiones.

## 3. Alternativa económica (demo): una sola EC2

Para una demostración sin MSK/RDS/ECS:

```bash
# EC2 Amazon Linux 2023, t3.xlarge (4 vCPU / 16 GB), disco 40 GB; Security Group: 22 (tu IP) y 8443
sudo dnf install -y docker git && sudo systemctl enable --now docker
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
git clone https://github.com/Francisca-Valenzuela/Exp1_S1_Grupo5.git && cd Exp1_S1_Grupo5
sudo docker compose up -d --build
```
Probar desde tu equipo con los endpoints de `instrucciones.md`, reemplazando `localhost` por la IP pública de la instancia.
Apagar la instancia al terminar para no generar costos.

## 4. Checklist de producción

- [ ] Secretos (BD, clientes OAuth) en Secrets Manager; rotar `internal-client`.
- [ ] MSK con TLS + autenticación IAM/SASL; ACL por servicio.
- [ ] Auth-server con claves de firma persistentes (KMS/Secrets) y varias réplicas.
- [ ] RDS Multi-AZ con *backups* y cifrado en reposo.
- [ ] WAF en el ALB; límite de tasa (rate limiting) en el gateway.
- [ ] Alarmas CloudWatch: errores 5xx, *consumer lag* de Kafka, circuitos abiertos, mensajes en `*.DLT`.
- [ ] Redis (ElastiCache) para el estado de retiros del cajero si se escala `bff-atm`.
