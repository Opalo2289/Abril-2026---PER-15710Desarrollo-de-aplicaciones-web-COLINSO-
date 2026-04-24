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
```

### Git Bash

```bash
chmod +x scripts/run.sh   # solo la primera vez
./scripts/run.sh list
./scripts/run.sh eureka-up
```

### Sin scripts (equivalente directo)

Desde `alquiler-vehiculos/`:

```bash
./mvnw -pl eureka-server spring-boot:run
```

## Comandos registrados (resumen)

| ID | Qué hace |
|----|------------|
| `eureka-up` | Arranca Eureka en el puerto **8761** (`http://localhost:8761`). |
| `compile-all` | Empaqueta todo el multi-módulo sin ejecutar tests. |
| `compile-eureka` | Compila solo `eureka-server`. |
| `vehiculos-up` | Arranca **vehiculos-service** en el puerto **8081** (necesita PostgreSQL y BD `vehiculos` o URL en env). |
| `compile-vehiculos` | Compila y ejecuta tests de `vehiculos-service` (H2 en perfil `test`). |
| `verify-versions` | Muestra `mvnw -v` (Java + Maven del wrapper). |

## Cómo añadir un comando nuevo

1. Abre **`scripts/registry.json`**.
2. Copia un bloque de `commands[]` y cambia `id`, `title`, `cwd` (si aplica) y los arrays `args` en `windows` y `unix`.
3. Comprueba con `run.ps1 -List` o `./scripts/run.sh list`.
4. Opcional: anota una línea en esta tabla para tu equipo.

Cuando existan más módulos (`vehiculos-service`, `operaciones-service`, etc.), añade aquí entradas como `vehiculos-up` apuntando a `-pl vehiculos-service spring-boot:run`.
