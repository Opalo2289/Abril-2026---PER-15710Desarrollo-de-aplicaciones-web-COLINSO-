# Alquiler de vehículos — microservicios (UNIR PER-15710)

Backend en **Java 17**, **Spring Boot 3.3** y **Spring Cloud** (Eureka, Gateway, OpenFeign, LoadBalancer). Cuatro componentes: **eureka-server**, **vehiculos-service**, **operaciones-service**, **api-gateway**.

## Estructura del repositorio

```
alquiler-vehiculos/
├── eureka-server/          # Registro Eureka (8761)
├── vehiculos-service/      # Catálogo CRUD + BD propia (8081)
├── operaciones-service/    # Solicitudes + BD propia + Feign a vehículos (8082)
├── api-gateway/            # Entrada única (8080)
├── docker-compose.yml      # Stack completo + 2× PostgreSQL
├── scripts/                # run.ps1, run.sh, registry.json, COMANDOS.md
└── docs/                   # Guía de memoria y checklist ZIP (lote 9)
```

## Requisitos

- **JDK 17** (el POM fija `java.version` 17; versiones superiores suelen funcionar con Maven).
- **Maven Wrapper** incluido (`mvnw`, `mvnw.cmd`).
- Para **Docker**: [Docker Desktop](https://www.docker.com/products/docker-desktop/) con soporte Linux.

## Arranque en desarrollo (sin Docker)

Abre **cuatro terminales** en la raíz `alquiler-vehiculos/` y ejecuta en este orden:

| Orden | Servicio            | Comando |
|------|---------------------|---------|
| 1    | Eureka              | `.\scripts\run.ps1 -Id eureka-up` o `./scripts/run.sh eureka-up` |
| 2    | Vehículos           | `.\scripts\run.ps1 -Id vehiculos-up` o `./scripts/run.sh vehiculos-up` |
| 3    | Operaciones         | `.\scripts\run.ps1 -Id operaciones-up` o `./scripts/run.sh operaciones-up` |
| 4    | API Gateway         | `.\scripts\run.ps1 -Id gateway-up` o `./scripts/run.sh gateway-up` |

Copia `scripts/env.local.example` a `scripts/.env.local` (no se versiona) y define `JAVA_HOME`, `VEHICULOS_DATASOURCE_*`, `OPERACIONES_DATASOURCE_*` y, si usas **Neon**, las credenciales en formato JDBC (ver [scripts/COMANDOS.md](scripts/COMANDOS.md)).

**No** mezcles `SPRING_DATASOURCE_*` en el mismo `.env` si arrancas vehículos y operaciones: Spring las aplica globalmente y operaciones podría conectar a la BD equivocada.

### URLs en desarrollo

| Qué | URL |
|-----|-----|
| Eureka | http://localhost:8761 |
| Gateway (entrada única) | http://localhost:8080 |
| Vehículos (directo) | http://localhost:8081 |
| Operaciones (directo) | http://localhost:8082 |
| Swagger vehículos | http://localhost:8081/swagger-ui.html |
| Swagger operaciones | http://localhost:8082/swagger-ui.html |

Ejemplos vía Gateway: `GET http://localhost:8080/vehiculos`, `GET http://localhost:8080/operaciones/solicitudes`.

## Arranque con Docker Compose

Desde `alquiler-vehiculos/`:

```bash
docker compose up --build -d
docker compose ps
```

Los contenedores **Postgres** del Compose son **independientes** de Neon; la primera vez las bases van vacías (`GET /vehiculos` puede devolver `[]`). Los datos persisten en volúmenes Docker hasta que ejecutes `docker compose down -v`.

Detalle de puertos, `down` vs `down -v` y datos de prueba: [scripts/COMANDOS.md](scripts/COMANDOS.md) (sección *Docker Compose*).

## Pruebas automatizadas

```bash
./mvnw test
```

O por módulo: `./mvnw -pl vehiculos-service test`, etc.

## Documentación OpenAPI (Swagger)

- **vehiculos-service**: http://localhost:8081/swagger-ui.html  
- **operaciones-service**: http://localhost:8082/swagger-ui.html  

El Gateway **no** expone Swagger unificado; el punto de entrada HTTP para clientes es el puerto **8080**.

## Entrega académica

- **Memoria en PDF** (máx. ~20 páginas, referencias **APA**): guía de apartados y referencias en [docs/memoria-entrega.md](docs/memoria-entrega.md).
- **ZIP del proyecto** según enunciado: checklist en el mismo archivo (código fuente, `docker-compose.yml`, sin secretos).

## Más ayuda

Comandos registrados y flujos detallados: [scripts/COMANDOS.md](scripts/COMANDOS.md).  
Progreso por lotes del curso: `../_meta/docs/progreso-lotes.md` (si el workspace es la raíz del repo del curso).
