# Account & Transaction Service (Imperative Core)

Este microservicio gestiona las operaciones bancarias centrales (creación de cuentas, consultas de saldo, depósitos y retiros). Forma parte de un ejercicio práctico de arquitectura híbrida (Imperativa + Reactiva) orientado a la experimentación con patrones de diseño, caché multinivel y comunicación gRPC inter-servicios.

> **📌 Repositorio Complementario:**  
> Este servicio requiere del servidor de telemetría reactivo para evaluar el riesgo y las comisiones de cada transacción.  
> 🔗 [Event Telemetry Service (gRPC Server / Reactive)](https://github.com/Juan-Barraza/event-telemtry-service)

---

## Arquitectura 

- **Paradigma:** Imperativo / Bloqueante (Spring MVC + Spring Data JPA).
- **Caché:** Redis (`@Cacheable` / `@CacheEvict`) para optimizar lecturas de información de cuentas.
- **Persistencia:** PostgreSQL con transacciones ACID (`@Transactional`).
- **Comunicación Saliente:** Cliente gRPC (`Protobuf`) hacia el servicio de telemetría.

---

## Requisitos Previos y Ejecución Conjunta

Este microservicio está diseñado para ser orquestado mediante **Docker Compose** junto a sus dependencias de infraestructura y el servicio de telemetría.

### Variables de Entorno Para Este MicroServicio

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `SERVER_PORT_ACCOUNT` | Puerto HTTP del servicio | `8080` |
| `POSTGRES_HOST` | Host de la base de datos | `postgres` |
| `POSTGRES_PORT` | Puerto de PostgreSQL | `5432` |
| `POSTGRES_USER` | Usuerio de PostgresSQL | `postgres` |
| `POSTGRES_PASSWORD` | Passowrd de PostgresSQL | `password`|
| `POSTGRES_DB` | Nombre de la base de datos | `account_db` |
| `REDIS_PORT` | Puerto de la cache | `6379` | 
| `REDIS_HOST` | Host de la caché | `redis` |
| `TELEMETRY_GRPC_HOST` | Host del servicio gRPC de Telemetría | `telemetry-service` |
| `TELEMETRY_GRPC_PORT` | Puerto gRPC de Telemetría | `9090` |

### Despliegue con Docker Compose

Desde la raíz del proyecto orquestador (donde se encuentra el `docker-compose.yml` unificado):

```bash 
docker-compose up --build
```


## Documentación de API (REST)
Una vez levantado el servicio, la documentación interactiva de Swagger UI está disponible a nivel de desarrollo en:

`http://localhost:8080/swagger-ui.html` 

### Endpoints Principales
* `POST /api/v1/accounts` - Creación de cuentas bancarias.

* `GET /api/v1/accounts/{accountNumber}` - Consulta de saldo (con caché Redis).

* `POST /api/v1/transactions` - Procesamiento de transacciones (evaluadas vía gRPC).

* `GET /api/v1/transactions/{accountNumber}` - Historial paginado de transacciones.