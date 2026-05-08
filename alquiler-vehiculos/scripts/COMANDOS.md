# Comandos del proyecto — alquiler-vehiculos

Todo lo repetible vive en **`registry.json`**. Los scripts **`run.ps1`** (Windows / PowerShell) y **`run.sh`** (Git Bash / Linux / macOS) leen ese archivo: al añadir un comando nuevo al JSON, aparece al listar y se puede ejecutar sin duplicar líneas largas.

## Requisitos

- **JDK** instalado (17 recomendado en el POM; en la práctica suele funcionar con 21/22).
- **Maven Wrapper** en la raíz de `alquiler-vehiculos/` (`mvnw`, `mvnw.cmd`, carpeta `.mvn/`).
- Para **`run.sh`**: en **Git Bash sobre Windows** no hace falta Python (delega en `run.ps1`). En **Linux / macOS** hace falta **`python3`** (o `python`) para leer el JSON.

## Variables locales (opcional)

1. Copia `env.local.example` → **`scripts/.env.local`**.
2. Define ahí `JAVA_HOME`, credenciales de BD cuando existan, etc.
3. `run.ps1` y `run.sh` cargan automáticamente `.env.local` si existe.

## Uso rápido

### PowerShell (desde la raíz `alquiler-vehiculos/`)

```powershell
.\scripts\run.ps1 -List
.\scripts\run.ps1 -Id eureka-up
.\scripts\run.ps1 -Id vehiculos-up
```

### Git Bash

```bash
chmod +x scripts/run.sh   # solo la primera vez
./scripts/run.sh list
./scripts/run.sh eureka-up
./scripts/run.sh vehiculos-up
```

## Levantar Eureka, microservicios y API Gateway (desarrollo)

Son **cuatro procesos** distintos desde el lote 7; abre **cuatro terminales** desde la raíz **`alquiler-vehiculos/`**.

> **Lote 6 — orden obligatorio (microservicios):** `operaciones-service` se registra en **Eureka** y descubre `vehiculos-service` por nombre vía Spring Cloud LoadBalancer (sin URL fija). Debes levantar **Eureka → vehículos → operaciones** antes del Gateway.

> **Lote 7 — API Gateway:** Tras tener **VEHICULOS-SERVICE** y **OPERACIONES-SERVICE** visibles en `http://localhost:8761`, arranca **api-gateway** (puerto **8080**). El cliente externo puede usar solo `http://localhost:8080/vehiculos/...` y `http://localhost:8080/operaciones/...`; el Gateway reescribe internamente a `/api/vehiculos/...` y `/api/operaciones/...`. **Swagger UI** sigue en los puertos **8081** y **8082** de cada microservicio.

| Orden | Servicio | Puerto | Comando (recomendado, carga `scripts/.env.local`) |
|-------|-----------|--------|---------------------------------------------------|
| 1 | **Eureka** | **8761** | `.\scripts\run.ps1 -Id eureka-up` o `./scripts/run.sh eureka-up` |
| 2 | **vehiculos-service** | **8081** | `.\scripts\run.ps1 -Id vehiculos-up` o `./scripts/run.sh vehiculos-up` |
| 3 | **operaciones-service** | **8082** | `.\scripts\run.ps1 -Id operaciones-up` o `./scripts/run.sh operaciones-up` |
| 4 | **api-gateway** | **8080** | `.\scripts\run.ps1 -Id gateway-up` o `./scripts/run.sh gateway-up` |

**vehiculos-service** usa variables **`VEHICULOS_DATASOURCE_*`** (o, si solo ese servicio, el fallback **`SPRING_DATASOURCE_*`** documentado en `application.yml`). **operaciones-service** usa **`OPERACIONES_DATASOURCE_*`** (otro proyecto Neón, ej. UNIR-BD-2-OPERACIONES). **No pongas `SPRING_DATASOURCE_*` junto con operaciones**: Spring Boot las interpreta como `spring.datasource` global y **`operaciones-service` ignoraría su URL** y hablaría con la misma BD que vehículos. En **Windows**, si `mvnw.cmd` pide Java, define también **`JAVA_HOME`** en ese mismo archivo (ver `env.local.example`).

**URLs útiles:** `http://localhost:8761` (Eureka; con todo arriba: **VEHICULOS-SERVICE**, **OPERACIONES-SERVICE**, **API-GATEWAY**), `http://localhost:8080/actuator/health` (Gateway), `http://localhost:8081/swagger-ui.html` (vehículos), `http://localhost:8081/actuator/health`, `http://localhost:8082/swagger-ui.html` (operaciones), `http://localhost:8082/actuator/health`.

**Entrada única (lote 7):** `http://localhost:8080/vehiculos` y `http://localhost:8080/operaciones/solicitudes` (equivalentes a `/api/...` en 8081/8082).

Si **vehiculos-service** arranca **sin** Eureka en marcha, verás errores de conexión en el log hasta que levantes Eureka o desactives el cliente con variables propias; en desarrollo normal levanta primero Eureka y luego vehículos.

### Sin scripts (`mvnw` directo)

Misma carpeta `alquiler-vehiculos/`. En Windows conviene tener `JAVA_HOME` y las variables de BD en el entorno **antes** de ejecutar (o usa `run.ps1`, que lee `.env.local`):

```bash
./mvnw -pl eureka-server spring-boot:run
```

```bash
./mvnw -pl vehiculos-service spring-boot:run
```

```bash
./mvnw -pl operaciones-service spring-boot:run
```

```bash
./mvnw -pl api-gateway spring-boot:run
```

## Pruebas Postman vía Gateway (lote 7)

**Prerrequisito:** los cuatro procesos en orden (Eureka → vehículos → operaciones → gateway). Cabecera `Content-Type: application/json` en los POST.

| Qué | Método | URL |
|-----|--------|-----|
| Lista vehículos | GET | `http://localhost:8080/vehiculos` |
| Vehículo por id | GET | `http://localhost:8080/vehiculos/{id}` |
| Crear vehículo | POST | `http://localhost:8080/vehiculos` |
| Lista solicitudes | GET | `http://localhost:8080/operaciones/solicitudes` |
| Registrar solicitud | POST | `http://localhost:8080/operaciones/solicitudes` |
| Health Gateway | GET | `http://localhost:8080/actuator/health` |

**Cuerpo POST vehículo (ejemplo):** `marca`, `modelo`, `matricula`, `estado` (p. ej. `DISPONIBLE`), `precioPorDia` — mismo contrato que Swagger en `:8081`.

**Cuerpo POST solicitud (ejemplo):** `{ "vehiculoId": 1, "fechaInicio": "2030-07-01", "fechaFin": "2030-07-05" }`.

**Comparación:** la misma petición contra `http://localhost:8081/api/vehiculos` y `http://localhost:8080/vehiculos` debe dar el mismo código HTTP y cuerpo equivalente (idem operaciones con `/api/operaciones/...`).

### Criterios de éxito (lote 7)

1. `./mvnw -pl api-gateway test` → BUILD SUCCESS.
2. En Eureka aparecen **tres** aplicaciones: vehículos, operaciones y **API-GATEWAY**.
3. `GET http://localhost:8080/actuator/health` → `UP`.
4. GET/POST vía `:8080/vehiculos...` y `:8080/operaciones...` equivalentes a llamadas directas a 8081/8082.
5. Flujo end-to-end solo usando **8080** (crear vehículo DISPONIBLE → registrar solicitud) funciona.
6. Los MS siguen respondiendo directo en 8081 y 8082.

**Fallo típico:** Gateway antes que Eureka o sin instancias registradas → errores 503/502 o resolución `lb://` vacía.

## Comandos registrados (resumen)

| ID | Qué hace |
|----|------------|
| `eureka-up` | Arranca **eureka-server** en el puerto **8761** (`http://localhost:8761`). |
| `compile-all` | Empaqueta todo el multi-módulo sin ejecutar tests. |
| `compile-eureka` | Compila solo `eureka-server`. |
| `vehiculos-up` | Arranca **vehiculos-service** en el puerto **8081** (BD: local o Neon vía `scripts/.env.local`; en Windows suele hacer falta `JAVA_HOME` ahí). |
| `compile-vehiculos` | Compila y ejecuta tests de `vehiculos-service` (H2 en perfil `test`). |
| `operaciones-up` | Arranca **operaciones-service** en el puerto **8082** (BD `operaciones` u otra vía `OPERACIONES_DATASOURCE_*`). |
| `compile-operaciones` | Compila y ejecuta tests de `operaciones-service` (H2 en perfil `test`). |
| `gateway-up` | Arranca **api-gateway** en el puerto **8080** (requiere Eureka + MS registrados). |
| `compile-gateway` | Compila y ejecuta tests de `api-gateway` (perfil `test`, Gateway desactivado en test). |
| `verify-versions` | Muestra `mvnw -v` (Java + Maven del wrapper). |

## Cómo añadir un comando nuevo

1. Abre **`scripts/registry.json`**.
2. Copia un bloque de `commands[]` y cambia `id`, `title`, `cwd` (si aplica) y los arrays `args` en `windows` y `unix`.
3. Comprueba con `run.ps1 -List` o `./scripts/run.sh list`.
4. Opcional: anota una línea en esta tabla para tu equipo.

Para nuevos módulos, añade entradas en `registry.json` y una fila en la tabla de arriba.

## Neon (u otro PostgreSQL en la nube)

**No pegues la cadena `postgresql://...` con contraseña en este repo** (ni en `COMANDOS.md`): usa solo **`scripts/.env.local`**, que está en `.gitignore`.

Neon te da un URI tipo `postgresql://USUARIO:CONTRASEÑA@HOST/neondb?sslmode=require&...`. Para Spring Boot debes **partirlo en tres variables** en formato JDBC (véase [`vehiculos-service/.../application.yml`](../vehiculos-service/src/main/resources/application.yml) y [`operaciones-service/.../application.yml`](../operaciones-service/src/main/resources/application.yml)):

| Servicio | URL | Usuario | Contraseña |
|-----------|-----|---------|------------|
| **vehículos** | `VEHICULOS_DATASOURCE_URL` | `VEHICULOS_DATASOURCE_USERNAME` | `VEHICULOS_DATASOURCE_PASSWORD` |
| **operaciones** | `OPERACIONES_DATASOURCE_URL` | `OPERACIONES_DATASOURCE_USERNAME` | `OPERACIONES_DATASOURCE_PASSWORD` |

Ejemplo en **`scripts/.env.local`** (formato `KEY=valor`, una por línea; **sin** `export`: así lo lee `run.ps1` y Git Bash al delegar). Sustituye host, usuario y contraseña:

```text
VEHICULOS_DATASOURCE_URL=jdbc:postgresql://HOST:5432/neondb?sslmode=require
VEHICULOS_DATASOURCE_USERNAME=tu_usuario_neon
VEHICULOS_DATASOURCE_PASSWORD=tu_contraseña_neon
```

En Linux/macOS, si ejecutas `mvnw` a mano sin `run.ps1`, puedes `export` las mismas variables en tu shell o usar `set -a; source .env.local`.

El **host** es el segmento entre `@` y la siguiente `/` en el URI de Neon (sin `postgresql://` ni usuario/contraseña). El nombre de la base suele ser `neondb` u otra que elijas en el panel.

Si esa contraseña llegó a guardarse en un archivo versionado, **rótala** en [Neon Console](https://console.neon.tech) y actualiza solo `.env.local`.

**Fallo al arrancar operaciones (o vehículos) con Neon:** si en el log aparece `ERROR: Control plane request failed` o Hibernate *Unable to determine Dialect without JDBC metadata*, casi siempre falló la conexión JDBC: revisa que **`OPERACIONES_DATASOURCE_PASSWORD`** (o la de vehículos) **no esté vacía** en `.env.local` (una línea `PASSWORD=` sin valor hace que Spring envíe contraseña vacía a Neon). Comprueba en el panel de Neon que el proyecto esté activo y que URL, usuario y contraseña coincidan con la conexión JDBC del branch correcto.