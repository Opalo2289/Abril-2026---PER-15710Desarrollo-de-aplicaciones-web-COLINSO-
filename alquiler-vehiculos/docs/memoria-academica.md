# Sistema de Alquiler de Vehículos mediante Arquitectura de Microservicios

**Asignatura:** PER-15710 — Desarrollo de Aplicaciones Web  
**Institución:** Universidad Internacional de La Rioja (UNIR)  
**Proyecto:** Actividad 1 — Implementación de microservicios con Spring Boot y Spring Cloud  
**Fecha:** Mayo de 2026  
**Repositorio:** [github.com/Opalo2289/Abril-2026---PER-15710Desarrollo-de-aplicaciones-web-COLINSO-](https://github.com/Opalo2289/Abril-2026---PER-15710Desarrollo-de-aplicaciones-web-COLINSO-.git)  
**Rama activa:** `dev`
**Desarrollado por:** Guedys Enrique Angola Maestre  

---

---

## Índice

1. [Introducción](#1-introducción)  
   1.1. [Objetivo del sistema](#11-objetivo-del-sistema)  
   1.2. [Motivación y justificación de la temática](#12-motivación-y-justificación-de-la-temática)  
2. [Arquitectura del sistema](#2-arquitectura-del-sistema)  
   2.1. [Componentes principales](#21-componentes-principales)  
   2.2. [Diagrama de comunicación entre servicios](#22-diagrama-de-comunicación-entre-servicios)  
   2.3. [API Gateway y enrutamiento](#23-api-gateway-y-enrutamiento)  
   2.4. [Despliegue con Docker Compose](#24-despliegue-con-docker-compose)  
3. [Microservicio *vehículos*](#3-microservicio-vehículos)  
   3.1. [Modelo de datos](#31-modelo-de-datos)  
   3.2. [Documentación de la API REST](#32-documentación-de-la-api-rest)  
   3.3. [Códigos de respuesta HTTP](#33-códigos-de-respuesta-http)  
   3.4. [Ejemplos de interacción](#34-ejemplos-de-interacción)  
4. [Microservicio *operaciones*](#4-microservicio-operaciones)  
   4.1. [Modelo de datos](#41-modelo-de-datos)  
   4.2. [Documentación de la API REST](#42-documentación-de-la-api-rest)  
   4.3. [Ciclo de vida de una solicitud](#43-ciclo-de-vida-de-una-solicitud)  
   4.4. [Integración con el microservicio *vehículos*](#44-integración-con-el-microservicio-vehículos)  
   4.5. [Validación de solapes temporales](#45-validación-de-solapes-temporales)  
   4.6. [Códigos de respuesta HTTP](#46-códigos-de-respuesta-http)  
   4.7. [Ejemplos de interacción](#47-ejemplos-de-interacción)  
5. [Conclusiones](#5-conclusiones)  
6. [Referencias](#6-referencias)  

---

## 1. Introducción

### 1.1. Objetivo del sistema

El presente proyecto tiene como objetivo el diseño e implementación de un sistema de gestión de alquiler de vehículos sustentado en una arquitectura de microservicios. El sistema permite administrar el catálogo de vehículos disponibles y gestionar el ciclo de vida completo de las solicitudes de alquiler, desde su registro inicial hasta su confirmación o cancelación.

La solución se articula en torno a dos microservicios de negocio —**vehiculos-service** y **operaciones-service**— que operan de forma independiente, disponen de su propia base de datos y se comunican exclusivamente a través de interfaces HTTP bien definidas. Dichos servicios se exponen al exterior a través de un componente **API Gateway** que actúa como punto de entrada único, y se coordinan mediante un registro de servicios **Eureka** que implementa el patrón de descubrimiento de servicios (*service discovery*).

Desde el punto de vista tecnológico, el sistema está construido con **Java 17**, **Spring Boot 3.3** y el ecosistema **Spring Cloud**, empleando OpenFeign como cliente HTTP declarativo, Spring Cloud LoadBalancer para el balanceo de carga del lado del cliente y Springdoc OpenAPI 3 para la generación automática de documentación interactiva (Swagger UI).

### 1.2. Motivación y justificación de la temática

El dominio del alquiler de vehículos resulta especialmente adecuado para ilustrar los principios fundamentales de la arquitectura de microservicios por varias razones:

**Separación natural de responsabilidades.** Las entidades de negocio —el catálogo de vehículos y las solicitudes de alquiler— presentan ciclos de vida independientes y pueden evolucionar de forma autónoma. El microservicio *vehículos* gestiona datos de carácter relativamente estático (marcas, modelos, matrículas y precios), mientras que el microservicio *operaciones* gestiona transacciones con alta frecuencia de escritura y reglas de negocio complejas, como la detección de solapes temporales.

**Interoperabilidad controlada.** El sistema requiere que *operaciones* consulte el estado de disponibilidad de un vehículo en el catálogo antes de registrar una solicitud, lo que ilustra de forma práctica la comunicación síncrona entre microservicios mediante Feign y el papel del registro Eureka en el descubrimiento dinámico de instancias.

**Escalabilidad diferenciada.** En un entorno de producción real, la carga sobre el servicio de operaciones puede ser significativamente mayor que sobre el catálogo de vehículos, lo que justifica la capacidad de escalar ambos servicios de forma independiente, una ventaja característica de la arquitectura de microservicios frente al monolito tradicional.

**Relevancia curricular.** La problemática del alquiler de vehículos permite abordar de manera integral los contenidos de la asignatura: diseño de APIs REST, gestión de bases de datos relacionales con Spring Data JPA, contenedorización con Docker, descubrimiento de servicios con Eureka y comunicación inter-servicios con OpenFeign.

---

## 2. Arquitectura del sistema

### 2.1. Componentes principales

El sistema está compuesto por cuatro módulos Maven desplegados como contenedores Docker independientes:

| Componente | Artefacto Maven | Puerto | Responsabilidad |
|---|---|---|---|
| Registro de servicios | `eureka-server` | 8761 | Descubrimiento y registro de instancias (Eureka) |
| Catálogo de vehículos | `vehiculos-service` | 8081 | CRUD del catálogo y búsquedas por criterios |
| Gestión de operaciones | `operaciones-service` | 8082 | Registro y ciclo de vida de solicitudes de alquiler |
| Puerta de enlace | `api-gateway` | 8080 | Enrutamiento, punto de entrada único para clientes externos |

Cada microservicio de negocio dispone de su propia instancia de **PostgreSQL 16**, garantizando el principio de aislamiento de datos (*database per service*), uno de los pilares de la arquitectura de microservicios (Richardson, 2018). Las bases de datos no comparten tablas ni esquemas entre sí; toda interacción entre *operaciones-service* y *vehiculos-service* se produce exclusivamente mediante llamadas HTTP.

### 2.2. Diagrama de comunicación entre servicios

```
Cliente externo (HTTP)
        │
        ▼
  ┌─────────────┐
  │ api-gateway │  :8080
  └──────┬──────┘
         │  lb://vehiculos-service   │  lb://operaciones-service
         ▼                           ▼
  ┌──────────────┐           ┌──────────────────┐
  │  vehiculos-  │  ◄─Feign─ │  operaciones-    │
  │  service     │           │  service         │
  │  :8081       │           │  :8082           │
  └──────┬───────┘           └────────┬─────────┘
         │                            │
         ▼                            ▼
  ┌──────────────┐           ┌──────────────────┐
  │  PostgreSQL  │           │  PostgreSQL       │
  │  vehiculos   │           │  operaciones      │
  │  :5433       │           │  :5434            │
  └──────────────┘           └──────────────────┘
         ▲                            ▲
         └──────── eureka-server ─────┘
                      :8761
```

Todos los servicios se registran en Eureka al arrancar. Cuando *operaciones-service* necesita consultar el catálogo, OpenFeign resuelve la dirección del *vehiculos-service* mediante el registro de Eureka, sin URL fija en el código de producción.

### 2.3. API Gateway y enrutamiento

El API Gateway, implementado con **Spring Cloud Gateway**, centraliza el enrutamiento y abstrae la topología interna del sistema. Las rutas públicas se mapean a los prefijos internos de cada microservicio según la siguiente tabla:

| Ruta pública (Gateway :8080) | Ruta interna (microservicio) | Servicio destino |
|---|---|---|
| `GET /vehiculos` | `GET /api/vehiculos` | vehiculos-service |
| `GET /vehiculos/{id}` | `GET /api/vehiculos/{id}` | vehiculos-service |
| `POST /vehiculos` | `POST /api/vehiculos` | vehiculos-service |
| `PUT /vehiculos/{id}` | `PUT /api/vehiculos/{id}` | vehiculos-service |
| `DELETE /vehiculos/{id}` | `DELETE /api/vehiculos/{id}` | vehiculos-service |
| `GET /operaciones/**` | `GET /api/operaciones/**` | operaciones-service |
| `POST /operaciones/**` | `POST /api/operaciones/**` | operaciones-service |

El prefijo `/api` es un detalle de implementación interno y no queda expuesto al cliente externo. El balanceo de carga se realiza en el lado del cliente mediante Spring Cloud LoadBalancer, que selecciona entre las instancias disponibles registradas en Eureka.

### 2.4. Despliegue con Docker Compose

El archivo `docker-compose.yml` orquesta el despliegue completo del sistema. Cada servicio se construye a partir de su propio `Dockerfile` con imagen base `eclipse-temurin:17-jre-alpine`. Las dependencias entre contenedores se gestionan mediante *health checks*, de modo que *vehiculos-service* no arranca hasta que PostgreSQL del catálogo esté operativo, y *operaciones-service* espera a que tanto su PostgreSQL como *vehiculos-service* estén disponibles.

Las credenciales de base de datos se inyectan en tiempo de ejecución mediante variables de entorno, sin incluir valores sensibles en el código fuente ni en el control de versiones.

---

## 3. Microservicio *vehículos*

### 3.1. Modelo de datos

La entidad central del microservicio es `Vehiculo`, persistida en la tabla `vehiculos` de la base de datos dedicada. El esquema de la entidad es el siguiente:

| Campo | Tipo Java | Tipo SQL | Restricciones |
|---|---|---|---|
| `id` | `Long` | `BIGSERIAL` | Clave primaria, autoincremental |
| `marca` | `String` | `VARCHAR(80)` | NOT NULL |
| `modelo` | `String` | `VARCHAR(80)` | NOT NULL |
| `matricula` | `String` | `VARCHAR(20)` | NOT NULL, UNIQUE |
| `estado` | `EstadoVehiculo` | `VARCHAR(20)` | NOT NULL |
| `precioPorDia` | `BigDecimal` | `NUMERIC(12,2)` | Positivo, opcional |

El campo `estado` admite los siguientes valores del enumerado `EstadoVehiculo`:

| Valor | Descripción semántica |
|---|---|
| `DISPONIBLE` | El vehículo puede ser objeto de una nueva solicitud de alquiler |
| `ALQUILADO` | El vehículo se encuentra actualmente en uso por un cliente |
| `RESERVADO` | El vehículo tiene una reserva confirmada pendiente de inicio |
| `MANTENIMIENTO` | El vehículo está temporalmente fuera de servicio |

La unicidad de la matrícula se garantiza a nivel de base de datos mediante una restricción `UNIQUE CONSTRAINT` (`uk_vehiculos_matricula`). Un intento de crear un vehículo con matrícula duplicada provoca una respuesta `409 Conflict`.

### 3.2. Documentación de la API REST

La URL base interna del microservicio es `/api/vehiculos`; a través del Gateway, el prefijo público es `/vehiculos`. La documentación interactiva está disponible en `http://localhost:8081/swagger-ui.html`.

---

#### `GET /api/vehiculos`

**Descripción:** Devuelve el listado completo del catálogo o un subconjunto filtrado. Los parámetros de filtro se combinan con operador AND mediante Spring Data Specifications.

**Método HTTP:** `GET`  
**Autenticación:** No requerida  

**Parámetros de consulta (query params):**

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `marca` | `String` | No | Filtro por marca; coincidencia exacta, sin distinción de mayúsculas |
| `modelo` | `String` | No | Filtro por modelo; coincidencia exacta, sin distinción de mayúsculas |
| `estado` | `EstadoVehiculo` | No | Filtro por estado (`DISPONIBLE`, `ALQUILADO`, `RESERVADO`, `MANTENIMIENTO`) |

**Respuesta exitosa:** `200 OK` — Array JSON de objetos `VehiculoResponse`.

---

#### `GET /api/vehiculos/{id}`

**Descripción:** Recupera un vehículo concreto por su identificador interno.

**Método HTTP:** `GET`  
**Parámetros de ruta:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Identificador interno del vehículo |

**Respuestas:**

| Código | Descripción |
|---|---|
| `200 OK` | Objeto `VehiculoResponse` |
| `404 Not Found` | No existe ningún vehículo con el `id` indicado |

---

#### `POST /api/vehiculos`

**Descripción:** Crea un nuevo vehículo en el catálogo.

**Método HTTP:** `POST`  
**Content-Type:** `application/json`  

**Cuerpo de la petición (`VehiculoRequest`):**

| Campo | Tipo | Requerido | Restricciones |
|---|---|---|---|
| `marca` | `String` | Sí | No vacío, máx. 80 caracteres |
| `modelo` | `String` | Sí | No vacío, máx. 80 caracteres |
| `matricula` | `String` | Sí | No vacío, máx. 20 caracteres, único |
| `estado` | `EstadoVehiculo` | Sí | Uno de los valores del enumerado |
| `precioPorDia` | `BigDecimal` | No | Valor positivo |

**Respuestas:**

| Código | Descripción |
|---|---|
| `201 Created` | Vehículo creado; devuelve el objeto `VehiculoResponse` completo |
| `400 Bad Request` | Error de validación en el cuerpo de la petición |
| `409 Conflict` | Matrícula ya existente en el catálogo |

---

#### `PUT /api/vehiculos/{id}`

**Descripción:** Actualiza en su totalidad los datos de un vehículo existente (reemplazo completo).

**Método HTTP:** `PUT`  
**Content-Type:** `application/json`  
**Parámetros de ruta:** `id` (Long) — Identificador del vehículo a actualizar.  
**Cuerpo de la petición:** idéntico al de `POST /api/vehiculos`.

**Respuestas:**

| Código | Descripción |
|---|---|
| `200 OK` | Vehículo actualizado; devuelve el objeto `VehiculoResponse` |
| `400 Bad Request` | Error de validación |
| `404 Not Found` | Vehículo no encontrado |
| `409 Conflict` | Matrícula en conflicto con otro vehículo |

---

#### `DELETE /api/vehiculos/{id}`

**Descripción:** Elimina permanentemente un vehículo del catálogo.

**Método HTTP:** `DELETE`  
**Parámetros de ruta:** `id` (Long) — Identificador del vehículo.

**Respuestas:**

| Código | Descripción |
|---|---|
| `204 No Content` | Vehículo eliminado correctamente |
| `404 Not Found` | Vehículo no encontrado |

---

### 3.3. Códigos de respuesta HTTP

| Código | Situación |
|---|---|
| `200 OK` | Operación de lectura o actualización exitosa |
| `201 Created` | Recurso creado correctamente (POST) |
| `204 No Content` | Recurso eliminado correctamente (DELETE) |
| `400 Bad Request` | Violación de restricciones de validación (`@Valid`) |
| `404 Not Found` | Recurso no encontrado (`VehiculoNotFoundException`) |
| `409 Conflict` | Matrícula duplicada (`MatriculaDuplicadaException`) |

Los errores son gestionados de forma centralizada por `GlobalExceptionHandler`, que devuelve respuestas JSON homogéneas con el código HTTP apropiado.

### 3.4. Ejemplos de interacción

**Ejemplo 1: Listar vehículos disponibles**

```http
GET /vehiculos?estado=DISPONIBLE HTTP/1.1
Host: localhost:8080
```

```json
[
  {
    "id": 1,
    "marca": "Toyota",
    "modelo": "Corolla",
    "matricula": "1234-ABC",
    "estado": "DISPONIBLE",
    "precioPorDia": 45.50
  },
  {
    "id": 3,
    "marca": "Seat",
    "modelo": "León",
    "matricula": "9876-XYZ",
    "estado": "DISPONIBLE",
    "precioPorDia": 38.00
  }
]
```

**Ejemplo 2: Crear un vehículo**

```http
POST /vehiculos HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "marca": "Volkswagen",
  "modelo": "Golf",
  "matricula": "5678-DEF",
  "estado": "DISPONIBLE",
  "precioPorDia": 52.00
}
```

```json
HTTP/1.1 201 Created

{
  "id": 4,
  "marca": "Volkswagen",
  "modelo": "Golf",
  "matricula": "5678-DEF",
  "estado": "DISPONIBLE",
  "precioPorDia": 52.00
}
```

**Ejemplo 3: Vehículo no encontrado**

```http
GET /vehiculos/999 HTTP/1.1
Host: localhost:8080
```

```json
HTTP/1.1 404 Not Found

{
  "status": 404,
  "error": "Not Found",
  "message": "Vehículo con id 999 no encontrado"
}
```

---

## 4. Microservicio *operaciones*

### 4.1. Modelo de datos

La entidad central del microservicio es `Solicitud`, persistida en la tabla `solicitudes` de su base de datos dedicada. Es fundamental destacar que esta tabla **no contiene claves foráneas** que apunten a la base de datos de *vehiculos-service*; la relación se establece únicamente mediante el campo `vehiculoId`, que actúa como referencia lógica. Esto respeta el principio de aislamiento de datos en arquitecturas de microservicios.

| Campo | Tipo Java | Tipo SQL | Restricciones |
|---|---|---|---|
| `id` | `Long` | `BIGSERIAL` | Clave primaria, autoincremental |
| `vehiculoId` | `Long` | `BIGINT` | NOT NULL; referencia lógica al catálogo |
| `fechaInicio` | `LocalDate` | `DATE` | NOT NULL |
| `fechaFin` | `LocalDate` | `DATE` | NOT NULL |
| `estado` | `EstadoSolicitud` | `VARCHAR(20)` | NOT NULL |

El campo `estado` admite los valores del enumerado `EstadoSolicitud`:

| Valor | Descripción |
|---|---|
| `PENDIENTE` | Estado inicial tras el registro; en espera de confirmación |
| `CONFIRMADA` | La solicitud ha sido aprobada |
| `CANCELADA` | La solicitud ha sido rechazada o anulada |

### 4.2. Documentación de la API REST

La URL base interna del microservicio es `/api/operaciones/solicitudes`; a través del Gateway, el prefijo público es `/operaciones/solicitudes`. La documentación interactiva está disponible en `http://localhost:8082/swagger-ui.html`.

---

#### `GET /api/operaciones/solicitudes`

**Descripción:** Devuelve el listado de solicitudes de alquiler, con filtro opcional por estado.

**Método HTTP:** `GET`  

**Parámetros de consulta:**

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `estado` | `EstadoSolicitud` | No | Filtrar por `PENDIENTE`, `CONFIRMADA` o `CANCELADA` |

**Respuesta exitosa:** `200 OK` — Array de objetos `SolicitudResponse`.

---

#### `GET /api/operaciones/solicitudes/{id}`

**Descripción:** Recupera una solicitud concreta por su identificador interno.

**Método HTTP:** `GET`  
**Parámetros de ruta:** `id` (Long) — Identificador de la solicitud.

**Respuestas:**

| Código | Descripción |
|---|---|
| `200 OK` | Objeto `SolicitudResponse` |
| `404 Not Found` | Solicitud no encontrada |

---

#### `POST /api/operaciones/solicitudes`

**Descripción:** Registra una nueva solicitud de alquiler. La solicitud se crea con estado `PENDIENTE`. Antes de persistirla, el sistema ejecuta dos validaciones: (1) consulta a *vehiculos-service* para verificar que el vehículo existe y tiene estado `DISPONIBLE`, y (2) comprueba que no existen solicitudes activas (PENDIENTE o CONFIRMADA) para el mismo vehículo que se solapen temporalmente con el rango de fechas solicitado.

**Método HTTP:** `POST`  
**Content-Type:** `application/json`  

**Cuerpo de la petición (`SolicitudRequest`):**

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `vehiculoId` | `Long` | Sí | ID del vehículo en el catálogo (*vehiculos-service*) |
| `fechaInicio` | `LocalDate` | Sí | Inicio del periodo (formato ISO-8601: `YYYY-MM-DD`) |
| `fechaFin` | `LocalDate` | Sí | Fin del periodo (formato ISO-8601: `YYYY-MM-DD`) |

**Respuestas:**

| Código | Descripción |
|---|---|
| `201 Created` | Solicitud registrada con estado `PENDIENTE` |
| `400 Bad Request` | Error de validación (campos nulos, fechas inválidas) |
| `404 Not Found` | Solicitud no encontrada |
| `409 Conflict` | Vehículo no disponible o solape de fechas detectado |

---

#### `POST /api/operaciones/solicitudes/{id}/confirmar`

**Descripción:** Realiza la transición de estado `PENDIENTE → CONFIRMADA`.

**Método HTTP:** `POST`  
**Parámetros de ruta:** `id` (Long) — Identificador de la solicitud.

**Respuestas:**

| Código | Descripción |
|---|---|
| `200 OK` | Solicitud confirmada; devuelve `SolicitudResponse` actualizado |
| `404 Not Found` | Solicitud no encontrada |
| `409 Conflict` | Transición de estado inválida (p. ej., la solicitud ya está CANCELADA) |

---

#### `POST /api/operaciones/solicitudes/{id}/cancelar`

**Descripción:** Realiza la transición de estado `PENDIENTE → CANCELADA`.

**Método HTTP:** `POST`  
**Parámetros de ruta:** `id` (Long) — Identificador de la solicitud.

**Respuestas:**

| Código | Descripción |
|---|---|
| `200 OK` | Solicitud cancelada; devuelve `SolicitudResponse` actualizado |
| `404 Not Found` | Solicitud no encontrada |
| `409 Conflict` | Transición de estado inválida |

---

### 4.3. Ciclo de vida de una solicitud

El diagrama de estados siguiente ilustra las transiciones permitidas para una solicitud de alquiler:

```
                  ┌─────────────────────────────────────┐
                  │       POST /solicitudes              │
                  │   (validación Feign + solapes)       │
                  ▼                                      │
           ┌────────────┐                                │
           │  PENDIENTE │                                │
           └─────┬──────┘                                │
                 │                                       │
       ┌─────────┴──────────┐                            │
       │ /confirmar          │ /cancelar                 │
       ▼                    ▼                            │
 ┌──────────────┐    ┌──────────────┐                   │
 │  CONFIRMADA  │    │  CANCELADA   │  (estado final)   │
 └──────────────┘    └──────────────┘                   │
```

Las transiciones `CONFIRMADA → PENDIENTE`, `CANCELADA → PENDIENTE` y cualquier otra combinación no contemplada devuelven `409 Conflict` mediante `TransicionEstadoInvalidaException`.

### 4.4. Integración con el microservicio *vehículos*

La comunicación entre *operaciones-service* y *vehiculos-service* se implementa mediante **OpenFeign**, el cliente HTTP declarativo de Spring Cloud. La interfaz `VehiculoCatalogoClient` declara la llamada remota:

```java
// Feign client — resolución de nombre por Eureka
@FeignClient(name = "vehiculos-service")
public interface VehiculoCatalogoClient {
    @GetMapping("/api/vehiculos/{id}")
    VehiculoCatalogoResponse obtener(@PathVariable Long id);
}
```

La clase `FeignDisponibilidadVehiculos` implementa la lógica de verificación: si el vehículo existe y su estado es `DISPONIBLE`, la solicitud puede continuar. Si el vehículo no existe (`404`) o su estado es distinto de `DISPONIBLE`, se lanza `VehiculoNoDisponibleException`, que el controlador traduce a una respuesta `409 Conflict`.

Este diseño preserva el principio de **autonomía de datos**: *operaciones-service* no realiza consultas directas a la base de datos de *vehiculos-service*; toda la información se obtiene a través de la API REST del otro microservicio.

La resolución del nombre `vehiculos-service` se realiza en tiempo de ejecución mediante el registro Eureka, lo que permite que el sistema sea resiliente ante cambios de dirección IP o la adición de nuevas instancias del catálogo.

### 4.5. Validación de solapes temporales

Cuando se registra una nueva solicitud, el sistema comprueba si ya existe alguna solicitud en estado `PENDIENTE` o `CONFIRMADA` para el mismo vehículo cuyo rango de fechas se solape con el rango solicitado. Esta comprobación se realiza mediante la siguiente condición lógica, ejecutada en la base de datos de operaciones:

```
solapamiento = NOT (fechaFin_nueva < fechaInicio_existente OR fechaInicio_nueva > fechaFin_existente)
```

Si se detecta un solape, se lanza `SolicitudSolapadaException` y la respuesta al cliente es `409 Conflict`. Esta validación garantiza la coherencia de las reservas sin necesidad de consultar en tiempo real al microservicio *vehículos* para este propósito específico.

### 4.6. Códigos de respuesta HTTP

| Código | Situación |
|---|---|
| `200 OK` | Operación de lectura, confirmación o cancelación exitosa |
| `201 Created` | Solicitud registrada correctamente |
| `400 Bad Request` | Validación fallida en el cuerpo de la petición |
| `404 Not Found` | Solicitud no encontrada (`SolicitudNotFoundException`) |
| `409 Conflict` | Vehículo no disponible, solape de fechas o transición de estado inválida |

### 4.7. Ejemplos de interacción

**Ejemplo 1: Registrar una solicitud**

```http
POST /operaciones/solicitudes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "vehiculoId": 1,
  "fechaInicio": "2030-07-01",
  "fechaFin": "2030-07-05"
}
```

```json
HTTP/1.1 201 Created

{
  "id": 10,
  "vehiculoId": 1,
  "fechaInicio": "2030-07-01",
  "fechaFin": "2030-07-05",
  "estado": "PENDIENTE"
}
```

**Ejemplo 2: Confirmar una solicitud**

```http
POST /operaciones/solicitudes/10/confirmar HTTP/1.1
Host: localhost:8080
```

```json
HTTP/1.1 200 OK

{
  "id": 10,
  "vehiculoId": 1,
  "fechaInicio": "2030-07-01",
  "fechaFin": "2030-07-05",
  "estado": "CONFIRMADA"
}
```

**Ejemplo 3: Solicitud rechazada por vehículo no disponible**

```http
POST /operaciones/solicitudes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "vehiculoId": 2,
  "fechaInicio": "2030-08-10",
  "fechaFin": "2030-08-15"
}
```

```json
HTTP/1.1 409 Conflict

{
  "status": 409,
  "error": "Conflict",
  "message": "El vehículo 2 no está disponible (estado actual: MANTENIMIENTO)"
}
```

**Ejemplo 4: Solicitud rechazada por solape de fechas**

```http
POST /operaciones/solicitudes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "vehiculoId": 1,
  "fechaInicio": "2030-07-03",
  "fechaFin": "2030-07-08"
}
```

```json
HTTP/1.1 409 Conflict

{
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una solicitud activa para el vehículo 1 en el rango de fechas indicado"
}
```

**Ejemplo 5: Listar solicitudes pendientes**

```http
GET /operaciones/solicitudes?estado=PENDIENTE HTTP/1.1
Host: localhost:8080
```

```json
HTTP/1.1 200 OK

[
  {
    "id": 10,
    "vehiculoId": 1,
    "fechaInicio": "2030-07-01",
    "fechaFin": "2030-07-05",
    "estado": "PENDIENTE"
  }
]
```

---

## 5. Conclusiones

El presente proyecto ha permitido implementar y documentar un sistema de alquiler de vehículos basado en una arquitectura de microservicios funcional, aplicando patrones y tecnologías de uso extendido en el desarrollo de aplicaciones empresariales contemporáneas.

Desde el punto de vista arquitectónico, la solución logra una separación clara de responsabilidades entre el microservicio de catálogo (*vehiculos-service*) y el de gestión de operaciones (*operaciones-service*), cada uno con su propia base de datos PostgreSQL. Esta independencia, uno de los principios fundamentales de la arquitectura de microservicios (Newman, 2019), facilita el mantenimiento, la evolución independiente y la escalabilidad diferenciada de ambos servicios.

La integración entre microservicios mediante **OpenFeign** y el descubrimiento dinámico de servicios a través de **Eureka** ilustra de forma práctica cómo construir sistemas distribuidos que sean resilientes ante cambios en la topología de red. La introducción de un **API Gateway** como punto de entrada único simplifica el consumo de la API por parte de los clientes externos, eliminando la necesidad de que estos conozcan las direcciones internas de cada microservicio.

En lo que respecta a las reglas de negocio, la validación de solapes temporales implementada en *operaciones-service* y la verificación del estado del vehículo mediante llamadas Feign garantizan la integridad de los datos sin violar el principio de aislamiento de bases de datos.

Como principales **dificultades** encontradas durante el desarrollo, cabe destacar: la configuración correcta de las variables de entorno para las múltiples fuentes de datos en un entorno con varios servicios simultáneos, la gestión de los perfiles de Spring (`!test`) para los clientes Feign en las pruebas unitarias con stubs, y el orden de arranque de los contenedores Docker garantizado mediante *health checks* encadenados.

Como posibles **mejoras futuras** se identifican: la incorporación de un mecanismo de *circuit breaker* (Resilience4j) para aumentar la resiliencia ante fallos del *vehiculos-service*, la autenticación y autorización centralizada en el Gateway mediante Spring Security y JWT, la implementación de un patrón Saga para gestionar transacciones distribuidas, y la habilitación de Swagger unificado en el Gateway para facilitar la exploración de toda la API desde un único punto.

---

## 6. Referencias

Carnell, J., & Sánchez, I. (2021). *Spring Microservices in Action* (2.ª ed.). Manning Publications.

Newman, S. (2019). *Monolith to Microservices: Evolutionary Patterns to Transform Your Monolith*. O'Reilly Media.

Richardson, C. (2018). *Microservices Patterns: With Examples in Java*. Manning Publications.

Spring Framework. (2024). *Spring Boot Reference Documentation (3.3)*. https://docs.spring.io/spring-boot/docs/3.3.x/reference/html/

Spring Cloud. (2024). *Spring Cloud Netflix — Eureka*. https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/

Spring Cloud. (2024). *Spring Cloud OpenFeign*. https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/

Spring Cloud. (2024). *Spring Cloud Gateway*. https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/

Springdoc. (2024). *springdoc-openapi — OpenAPI 3 Library for Spring Boot*. https://springdoc.org/

PostgreSQL Global Development Group. (2024). *PostgreSQL 16 Documentation*. https://www.postgresql.org/docs/16/

Docker Inc. (2024). *Docker Compose — Reference Documentation*. https://docs.docker.com/compose/

---

*Documento generado para entrega académica. Extensión: ~20 páginas en PDF con fuente 11 pt e interlineado 1,5.*
