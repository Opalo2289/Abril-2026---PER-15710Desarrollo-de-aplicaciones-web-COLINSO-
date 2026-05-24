# Frontend — Alquiler de Vehículos

SPA en **React 19 + Vite** que consume el API Gateway del backend (`http://localhost:8080`).

## Requisitos

- Node.js 18+
- Backend activo: Eureka, microservicios y **api-gateway** en el puerto **8080**

## Arranque en desarrollo

```bash
# Desde alquiler-vehiculos/frontend
cp .env.example .env
npm install
npm run dev
```

La aplicación queda en `http://localhost:5173`.

### Proxy de API

En desarrollo, las peticiones van a `VITE_API_BASE_URL` (por defecto `/api`). Vite reenvía `/api/*` al Gateway en `:8080` sin el prefijo `/api`:

| Front (axios) | Gateway |
|---------------|---------|
| `GET /api/vehiculos` | `GET /vehiculos` |
| `GET /api/operaciones/solicitudes` | `GET /operaciones/solicitudes` |

No hace falta CORS: el navegador ve mismo origen (`localhost:5173`).

## Scripts del monorepo

Desde `alquiler-vehiculos/scripts/`:

```bash
./run.sh frontend-dev      # npm run dev
./run.sh frontend-build    # npm run build
```

## Estructura

```
src/
  components/   # UI reutilizable (CSS Modules)
  pages/        # Rutas lazy-loaded
  hooks/        # useVehiculos, useSolicitudes
  services/     # Axios + integración REST
  styles/       # Variables y reset global
  constants/    # Enums de estados
```

## Rutas

| Ruta | Descripción |
|------|-------------|
| `/` | Inicio con estadísticas (`Promise.all`) |
| `/vehiculos` | Catálogo con filtros |
| `/vehiculos/:id` | Detalle + formulario de solicitud |
| `/admin` | CRUD de vehículos |
| `/operaciones` | Solicitudes: confirmar / cancelar |

## Verificación manual

1. Levantar backend (Docker o scripts `eureka-up`, `vehiculos-up`, `operaciones-up`, `gateway-up`).
2. `npm run dev` en `frontend/`.
3. Crear un vehículo en **Admin** (estado DISPONIBLE).
4. Registrar solicitud en detalle del vehículo.
5. Confirmar en **Operaciones** → el vehículo pasa a RESERVADO vía `PUT /vehiculos/{id}`.
6. Cancelar solicitud pendiente → vehículo vuelve a DISPONIBLE si estaba reservado.

## Build de producción

```bash
npm run build
npm run preview
```

Para producción sin proxy, configura `VITE_API_BASE_URL` apuntando al Gateway real (p. ej. `http://localhost:8080` si sirves el `dist/` en otro origen; en ese caso puede hacer falta CORS en el Gateway).

## Entrega académica

Incluir la carpeta `frontend/` en el ZIP **sin** `node_modules/` ni `dist/`.
