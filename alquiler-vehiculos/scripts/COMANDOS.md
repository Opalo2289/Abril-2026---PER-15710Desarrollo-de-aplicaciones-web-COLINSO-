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

## Levantar Eureka, vehiculos-service y operaciones-service (desarrollo)

Son **tres procesos** distintos desde el lote 5; abre **tres terminales** desde la raíz **`alquiler-vehiculos/`**.

> **Lote 5 — orden obligatorio:** `operaciones-service` consulta el estado del vehículo en `vehiculos-service` al registrar cada solicitud. Debes levantarlos en este orden: **Eureka → vehículos → operaciones**. Si `vehiculos-service` no está disponible cuando se registra una solicitud, recibirás un error 500. La URL se controla con `VEHICULOS_CLIENT_URL` (por defecto `http://localhost:8081`); ver `env.local.example`.

| Orden | Servicio | Puerto | Comando (recomendado, carga `scripts/.env.local`) |
|-------|-----------|--------|---------------------------------------------------|
| 1 | **Eureka** | **8761** | `.\scripts\run.ps1 -Id eureka-up` o `./scripts/run.sh eureka-up` |
| 2 | **vehiculos-service** | **8081** | `.\scripts\run.ps1 -Id vehiculos-up` o `./scripts/run.sh vehiculos-up` |
| 3 | **operaciones-service** | **8082** | `.\scripts\run.ps1 -Id operaciones-up` o `./scripts/run.sh operaciones-up` |

**vehiculos-service** usa variables **`VEHICULOS_DATASOURCE_*`** (o, si solo ese servicio, el fallback **`SPRING_DATASOURCE_*`** documentado en `application.yml`). **operaciones-service** usa **`OPERACIONES_DATASOURCE_*`** (otro proyecto Neón, ej. UNIR-BD-2-OPERACIONES). **No pongas `SPRING_DATASOURCE_*` junto con operaciones**: Spring Boot las interpreta como `spring.datasource` global y **`operaciones-service` ignoraría su URL** y hablaría con la misma BD que vehículos. En **Windows**, si `mvnw.cmd` pide Java, define también **`JAVA_HOME`** en ese mismo archivo (ver `env.local.example`).

**URLs útiles:** `http://localhost:8761` (panel Eureka; con vehículos arriba aparece **VEHICULOS-SERVICE**), `http://localhost:8081/swagger-ui.html` (vehículos), `http://localhost:8081/actuator/health`, `http://localhost:8082/swagger-ui.html` (operaciones), `http://localhost:8082/actuator/health`.

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