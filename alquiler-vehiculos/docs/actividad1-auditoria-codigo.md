# Actividad 1: Auditoría de código de una aplicación

**Asignatura:** PER-15710 — Desarrollo de Aplicaciones Web
**Institución:** Universidad Internacional de La Rioja (UNIR)
**Aplicación auditada:** Sistema de Alquiler de Vehículos (arquitectura de microservicios: `eureka-server`, `api-gateway`, `vehiculos-service`, `operaciones-service`)
**Repositorio:** [github.com/Opalo2289/Abril-2026---PER-15710Desarrollo-de-aplicaciones-web-COLINSO-](https://github.com/Opalo2289/Abril-2026---PER-15710Desarrollo-de-aplicaciones-web-COLINSO-.git)
**Rama analizada:** `dev`
**Herramienta de análisis estático:** Semgrep CLI 1.174.0 (reglas *community*: `p/java`, `p/owasp-top-ten`, `p/security-audit`, `p/secrets`)
**Desarrollado por:** Guedys Enrique Angola Maestre

---

## Índice

1. [Introducción](#1-introducción)
2. [Objetivos](#2-objetivos)
3. [Metodología](#3-metodología)
   3.1. [Herramienta de análisis estático empleada](#31-herramienta-de-análisis-estático-empleada)
   3.2. [Alcance del análisis](#32-alcance-del-análisis)
   3.3. [Proceso de verificación](#33-proceso-de-verificación)
4. [Resumen ejecutivo y nivel de riesgo](#4-resumen-ejecutivo-y-nivel-de-riesgo)
5. [Hallazgos detallados](#5-hallazgos-detallados)
   5.1 [CWE-306 — Ausencia de autenticación en los endpoints de negocio](#51-hallazgo-1--cwe-306--ausencia-de-autenticación-en-los-endpoints-de-negocio)
   5.2 [CWE-668 — Registro de servicios Eureka expuesto sin autenticación](#52-hallazgo-2--cwe-668--registro-de-servicios-eureka-expuesto-sin-autenticación)
   5.3 [CWE-798 — Credenciales de base de datos por defecto](#53-hallazgo-3--cwe-798--credenciales-de-base-de-datos-por-defecto)
   5.4 [CWE-209 — Fuga de información en mensajes de error no controlados](#54-hallazgo-4--cwe-209--fuga-de-información-en-mensajes-de-error-no-controlados)
   5.5 [CWE-200 — Exposición de detalles del Actuator](#55-hallazgo-5--cwe-200--exposición-de-detalles-del-actuator)
   5.6 [CWE-319 — Transmisión en texto claro](#56-hallazgo-6--cwe-319--transmisión-en-texto-claro)
   5.7 [CWE-915 — Asignación masiva en `VehiculoRequest.estado`](#57-hallazgo-7--cwe-915--asignación-masiva-en-vehiculorequestestado)
   5.8 [CWE-770 — Ausencia de límites de tasa (rate limiting)](#58-hallazgo-8--cwe-770--ausencia-de-límites-de-tasa-rate-limiting)
   5.9 [CWE-778 — Registro de auditoría insuficiente](#59-hallazgo-9--cwe-778--registro-de-auditoría-insuficiente)
   5.10 [CWE-250 — Contenedores ejecutados como root](#510-hallazgo-10--cwe-250--contenedores-ejecutados-como-root)
   5.11 [CWE-693 — Ausencia de cabeceras de seguridad HTTP](#511-hallazgo-11--cwe-693--ausencia-de-cabeceras-de-seguridad-http)
   5.12 [CWE-287 / CVE-2025-49146 — Bypass de channel binding en el driver JDBC de PostgreSQL](#512-hallazgo-12--cwe-287--cve-2025-49146--bypass-de-channel-binding-en-el-driver-jdbc-de-postgresql)
   5.13 [CWE-94 / CVE-2025-41243 — Inyección SpEL en Spring Cloud Gateway](#513-hallazgo-13--cwe-94--cve-2025-41243--inyección-spel-en-spring-cloud-gateway)
   5.14 [CWE-444 / CVE-2025-41235 — Reenvío de cabeceras `X-Forwarded-*` sin validar](#514-hallazgo-14--cwe-444--cve-2025-41235--reenvío-de-cabeceras-x-forwarded--sin-validar)
   5.15 [CWE-377 / CVE-2026-40973 — Directorio temporal predecible en Spring Boot](#515-hallazgo-15--cwe-377--cve-2026-40973--directorio-temporal-predecible-en-spring-boot)
6. [Conclusiones](#6-conclusiones)
7. [Bibliografía](#7-bibliografía)

---

## 1. Introducción

Toda aplicación software está expuesta a amenazas: actores, agentes o circunstancias con potencial de causar daño a sus datos, a su disponibilidad o a la organización que la opera. La revisión estática de código (*Static Application Security Testing*, SAST) es una práctica central del ciclo de vida de desarrollo seguro de software (S-SDLC), porque permite detectar errores de programación con implicaciones de seguridad **antes** de que el software llegue a producción, cuando el coste de corregirlos es más bajo.

El presente documento recoge la auditoría de código estático realizada sobre el sistema de **alquiler de vehículos**, una aplicación construida con **Java 17**, **Spring Boot 3.3.6** y **Spring Cloud 2023.0.4**, organizada en cuatro microservicios (`eureka-server`, `api-gateway`, `vehiculos-service`, `operaciones-service`) que se comunican mediante REST y descubrimiento de servicios. Se trata de una aplicación real, en desarrollo activo, y no de un banco de pruebas construido artificialmente con vulnerabilidades insertadas a propósito — lo que condiciona tanto el tipo de hallazgos posibles como la honestidad exigible en su reporte.

La auditoría combina el uso de una herramienta de análisis estático (Semgrep) con una revisión dirigida del código fuente, la configuración de despliegue y las versiones de dependencias declaradas en los `pom.xml`, siguiendo la práctica habitual en auditorías profesionales donde el SAST automatizado es el punto de partida, no el único instrumento de análisis.

## 2. Objetivos

- Analizar el código fuente de la aplicación de alquiler de vehículos para determinar el nivel de riesgo al que queda expuesta la organización a partir de las vulnerabilidades encontradas.
- Determinar las diversas vulnerabilidades de seguridad evidenciables en el código fuente, la configuración y las dependencias de la aplicación, clasificándolas según su código CWE (*Common Weakness Enumeration*).
- Proporcionar recomendaciones de solución concretas y aplicables al *stack* tecnológico empleado (Spring Boot / Spring Cloud) para mitigar el efecto de cada vulnerabilidad detectada.

## 3. Metodología

### 3.1. Herramienta de análisis estático empleada

Se empleó **Semgrep CLI v1.174.0**, una herramienta de SAST de código abierto con soporte nativo para Java y Spring, ampliamente utilizada en la industria (Snyk, GitLab y GitHub la integran como motor de *code scanning*). Se ejecutó contra la totalidad del monorepo con los siguientes conjuntos de reglas *community* del registro público de Semgrep:

```
semgrep scan --config=p/java --config=p/owasp-top-ten \
             --config=p/security-audit --config=p/secrets .
```

> **Nota sobre `p/spring`:** el conjunto de reglas `p/spring`, planeado inicialmente para esta auditoría, resultó estar **retirado del registro público de Semgrep** al momento de ejecutar el análisis (la petición de descarga de esa configuración devuelve HTTP 404). Se comprobó de forma aislada antes de excluirlo, para no invalidar el resto del comando combinado. Es un recordatorio de que los *rulesets* remotos de un SAST evolucionan con el tiempo y deben verificarse en cada ejecución, no darse por sentados de una auditoría a la siguiente.

**Resultado del escaneo automatizado: 0 hallazgos sobre 161 reglas aplicadas a 67 archivos.** Este resultado, en sí mismo, es información relevante para la auditoría: indica que el código de negocio no contiene los antipatrones sintácticos clásicos (concatenación de SQL, `eval` dinámico, deserialización insegura, etc.) que estas reglas de patrón detectan. No indica, sin embargo, que la aplicación esté libre de vulnerabilidades — las reglas de patrón de Semgrep no evalúan ausencias arquitectónicas (por ejemplo, que no exista ninguna capa de autenticación) ni vulnerabilidades conocidas en las versiones de dependencias declaradas, que son precisamente el origen de la mayoría de los hallazgos de esta auditoría.

**Evidencia y reproducibilidad.** La salida cruda de esta ejecución se conserva en el repositorio, junto a este informe, en dos formatos: [`semgrep-report.json`](./semgrep-report.json) (formato máquina, `--json`) y [`semgrep-report.txt`](./semgrep-report.txt) (formato consola, legible directamente). El comando es reproducible sin instalar nada del proyecto Java: basta `pip install --user semgrep` y `./scripts/run.sh security-audit` (o `.\scripts\run.ps1 -Id security-audit` en PowerShell) desde `alquiler-vehiculos/`, tal como se documenta en `scripts/COMANDOS.md`.

### 3.2. Alcance del análisis

Se revisaron los cuatro módulos Maven del monorepo (`eureka-server`, `api-gateway`, `vehiculos-service`, `operaciones-service`): su código Java (`domain`, `web`, `service`, `repository`, `client`, `config`), sus ficheros de configuración (`application.yml`, `application-docker.yml`), sus `Dockerfile`, el `docker-compose.yml` de orquestación y el árbol de dependencias declarado en los `pom.xml` (incluido el `pom.xml` padre). Se excluyeron del análisis los directorios `target/` (artefactos de compilación) y los recursos de test, por no formar parte del código desplegado en producción.

### 3.3. Proceso de verificación

Dado que el escaneo automatizado con reglas de patrón no arrojó hallazgos, la auditoría se completó con dos técnicas adicionales, también consideradas prácticas estándar de SAST en el S-SDLC:

1. **Revisión arquitectónica dirigida**: inspección manual de autenticación/autorización, validación de entrada, manejo de excepciones, construcción de consultas JPA, configuración de CORS, exposición de *actuator*, credenciales, cifrado en tránsito y registro de auditoría — contrastando cada hallazgo contra el código fuente real, citando archivo y línea.
2. **Análisis de composición de software (SCA)**: verificación de las versiones exactas de Spring Boot (3.3.6), Spring Cloud (2023.0.4 → Spring Cloud Gateway 4.1.6, OpenFeign 4.1.4), springdoc-openapi (2.6.0) y el driver JDBC de PostgreSQL (42.7.4, heredado del BOM de Spring Boot) contra los avisos oficiales de seguridad de Spring (`spring.io/security`) y GitHub Security Advisories (GHSA), citando el identificador CVE, la puntuación CVSS y la fuente de cada uno.

Todos los hallazgos de este documento han sido verificados por al menos una de estas dos vías directamente sobre el código del repositorio; ninguno se reporta por inferencia o generalización sin evidencia local.

## 4. Resumen ejecutivo y nivel de riesgo

De los 15 hallazgos documentados, **ninguno corresponde a una vulnerabilidad de inyección clásica** (SQLi, XSS reflejado, *command injection*) en el código de negocio: las consultas JPA están parametrizadas (Criteria API y JPQL con *named parameters*), no hay concatenación de cadenas SQL ni *native queries*, y no existe entrada de usuario reflejada sin escapar en ninguna respuesta HTML (la aplicación es una API REST sin vistas *server-side*). Esto es un resultado positivo real, no una omisión del análisis.

El riesgo real de la aplicación está concentrado en **tres ejes**:

| Eje de riesgo | Hallazgos relacionados | Nivel |
|---|---|---|
| Ausencia total de autenticación/autorización | #1, #2, #8, #11 | **Crítico** |
| Configuración por defecto insegura (credenciales, TLS, cabeceras, logs) | #3, #4, #5, #6, #9, #11 | **Alto** |
| Dependencias con CVE conocidos y sin parchear | #12, #13, #14, #15 | **Alto** (uno de ellos, #13, condicionalmente **Crítico**) |

El hallazgo más severo (#13, CVE-2025-41243, CVSS 10.0) no es explotable en la configuración *actual* del repositorio porque el *endpoint* de *actuator* `gateway` no está expuesto — pero la aplicación no tiene ninguna capa que impida que ese único parámetro de configuración cambie por error (por ejemplo, al depurar en un entorno no productivo). Combinado con la ausencia total de autenticación (hallazgo #1), el nivel de riesgo agregado para la organización es **alto**: cualquier persona con acceso de red a los puertos publicados por `docker-compose.yml` (8080, 8081, 8082, 8761) puede leer y modificar el catálogo de vehículos y las solicitudes de alquiler sin restricción alguna.

## 5. Hallazgos detallados

Cada hallazgo se documenta con: componente afectado, ubicación exacta en el código, evidencia, código CWE, severidad, impacto y recomendación de solución específica para el *stack* Spring Boot / Spring Cloud.

---

### 5.1. Hallazgo 1 — CWE-306 — Ausencia de autenticación en los endpoints de negocio

**Componente:** `vehiculos-service`, `operaciones-service`, `api-gateway`
**Severidad:** Crítica
**CWE:** [CWE-306: Missing Authentication for Critical Function](https://cwe.mitre.org/data/definitions/306.html)

**Evidencia.** Ninguno de los cuatro `pom.xml` del proyecto declara `spring-boot-starter-security` ni ninguna dependencia de autenticación equivalente (OAuth2, JWT). No existe en todo el árbol de código ninguna clase `SecurityConfig` ni `SecurityFilterChain`. En consecuencia, todos los métodos de `VehiculoController` (`vehiculos-service/src/main/java/com/alquiler/vehiculos/web/VehiculoController.java:36-72`) y de `SolicitudController` (`operaciones-service/src/main/java/com/alquiler/operaciones/web/SolicitudController.java:34-66`) quedan accesibles sin ninguna verificación de identidad ni de permisos:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest request) {
    return vehiculoService.crear(request);
}

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void eliminar(@PathVariable Long id) {
    vehiculoService.eliminar(id);
}
```

El `api-gateway` (`api-gateway/src/main/resources/application.yml:9-37`) reenvía el tráfico externo directamente a estos controladores mediante rutas `lb://vehiculos-service` y `lb://operaciones-service` sin ningún filtro de autenticación intermedio.

```
Cliente anónimo
     │  POST /vehiculos  (sin token, sin credenciales)
     ▼
API Gateway (:8080)  ── RewritePath ──►  vehiculos-service (:8081)
                                              │
                                              ▼
                                    VehiculoController.crear()
                                    (se ejecuta sin ninguna verificación)
```

**Impacto.** Cualquier actor con acceso de red a los puertos 8080/8081/8082 puede crear, modificar o eliminar vehículos, y registrar, confirmar o cancelar solicitudes de alquiler ajenas, sin necesidad de credenciales.

**Recomendación.** Incorporar `spring-boot-starter-security` en `vehiculos-service` y `operaciones-service`, definir un `SecurityFilterChain` que exija autenticación (JWT validado por el *gateway* es el patrón habitual en Spring Cloud Gateway) y aplicar autorización a nivel de método con `@PreAuthorize` para las operaciones de escritura (`POST`, `PUT`, `DELETE`). Como mínimo viable, centralizar la validación del token en el `api-gateway` mediante `TokenRelay` o un `GatewayFilter` de autenticación antes de reenviar la petición.

---

### 5.2. Hallazgo 2 — CWE-668 — Registro de servicios Eureka expuesto sin autenticación

**Componente:** `eureka-server`
**Severidad:** Alta
**CWE:** [CWE-668: Exposure of Resource to Wrong Sphere](https://cwe.mitre.org/data/definitions/668.html)

**Evidencia.** `eureka-server/src/main/resources/application.yml` no define ninguna configuración de seguridad, y el `pom.xml` de este módulo tampoco incluye `spring-boot-starter-security`. El *dashboard* y la API REST de registro de Eureka quedan expuestos en el puerto 8761, publicado directamente al host en `docker-compose.yml:46-47`.

**Impacto.** A diferencia del hallazgo #1 (que afecta a los datos de negocio), este hallazgo compromete la **integridad del propio mecanismo de descubrimiento de servicios**: un actor no autenticado con acceso al puerto 8761 puede consultar la metadata completa de todas las instancias registradas (*hostnames*, IPs, estado) y, en teoría, registrar una instancia falsa bajo el nombre de un servicio legítimo (por ejemplo, `vehiculos-service`), de forma que el `api-gateway` empiece a enrutar tráfico real hacia un servicio suplantado (*service registry poisoning*).

**Recomendación.** Proteger Eureka con `spring-boot-starter-security` y autenticación básica o mutua TLS entre los clientes Eureka y el servidor, tal como recomienda la documentación oficial de Spring Cloud Netflix para entornos que no sean de desarrollo local.

---

### 5.3. Hallazgo 3 — CWE-798 — Credenciales de base de datos por defecto

**Componente:** `vehiculos-service`, `operaciones-service`, `docker-compose.yml`
**Severidad:** Alta
**CWE:** [CWE-798: Use of Hard-coded Credentials](https://cwe.mitre.org/data/definitions/798.html)

**Evidencia.**

```yaml
# vehiculos-service/src/main/resources/application.yml:11-12
username: ${VEHICULOS_DATASOURCE_USERNAME:${SPRING_DATASOURCE_USERNAME:postgres}}
password: ${VEHICULOS_DATASOURCE_PASSWORD:${SPRING_DATASOURCE_PASSWORD:postgres}}
```

```yaml
# docker-compose.yml:8
POSTGRES_VEHICULOS_PASSWORD: ${POSTGRES_VEHICULOS_PASSWORD:-postgres}
```

El mismo patrón se repite en `operaciones-service/src/main/resources/application.yml:9-10` y en `docker-compose.yml:25` para la segunda base de datos. Si bien las credenciales son sobreescribibles por variable de entorno, el valor por defecto **committeado en el repositorio** es la pareja trivial `postgres`/`postgres`, y nada en el proyecto impide que ese valor por defecto llegue a un entorno real si no se define explícitamente la variable de entorno correspondiente.

**Impacto.** Despliegue con credenciales triviales y públicamente conocidas si no se sobreescriben explícitamente; riesgo de acceso no autorizado directo a la base de datos.

**Recomendación.** Eliminar el valor por defecto trivial de los ficheros versionados; exigir la variable de entorno sin *fallback* (`${VEHICULOS_DATASOURCE_PASSWORD}` sin `:postgres`), de forma que el arranque falle de forma explícita si no se ha configurado, y gestionar el secreto real mediante un *vault* (HashiCorp Vault, AWS Secrets Manager o, como mínimo, un fichero `.env` no versionado, tal como ya advierte el propio `env.local.example` del proyecto).

---

### 5.4. Hallazgo 4 — CWE-209 — Fuga de información en mensajes de error no controlados

**Componente:** `vehiculos-service`, `operaciones-service`
**Severidad:** Media
**CWE:** [CWE-209: Generation of Error Message Containing Sensitive Information](https://cwe.mitre.org/data/definitions/209.html)

**Evidencia.** Ambos `GlobalExceptionHandler` (`vehiculos-service/.../web/GlobalExceptionHandler.java` y `operaciones-service/.../web/GlobalExceptionHandler.java`) manejan correctamente las excepciones de negocio propias (`VehiculoNotFoundException`, `SolicitudSolapadaException`, etc.) devolviendo solo su mensaje, sin traza. Sin embargo, **ninguno de los dos define un `@ExceptionHandler(Exception.class)` de tipo catch-all**. Cualquier excepción no prevista (un `NullPointerException`, un fallo de conexión a base de datos, un error de Feign en `operaciones-service`) cae en el manejador por defecto de Spring Boot. Ninguno de los `application.yml` del proyecto configura `server.error.include-message`, `include-stacktrace` ni `include-exception` a `never` — son las propiedades que, en Spring Boot, controlan explícitamente si esos datos se filtran hacia el cliente.

**Impacto.** Ante un error no previsto, el cliente puede recibir el nombre de la clase de excepción y, según la configuración por defecto de Spring Boot en el perfil activo, el mensaje interno de la excepción — información que facilita el reconocimiento de la pila tecnológica y de posibles vectores de ataque adicionales.

**Recomendación.** Añadir un `@ExceptionHandler(Exception.class)` catch-all que devuelva un `ProblemDetail` genérico (HTTP 500, sin detalle interno) y registrar la excepción real solo en logs de servidor. Adicionalmente, fijar explícitamente en cada `application.yml`: `server.error.include-message: never`, `include-stacktrace: never`, `include-exception: false`.

---

### 5.5. Hallazgo 5 — CWE-200 — Exposición de detalles del Actuator

**Componente:** `eureka-server`
**Severidad:** Media
**CWE:** [CWE-200: Exposure of Sensitive Information to an Unauthorized Actor](https://cwe.mitre.org/data/definitions/200.html)

**Evidencia.**

```yaml
# eureka-server/src/main/resources/application.yml:23-30
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

A diferencia de los otros tres módulos, `eureka-server` fija `show-details: always` sobre el *endpoint* `health`. Combinado con el hallazgo #2 (ausencia total de autenticación en este módulo), cualquier actor no autenticado puede consultar el detalle interno de los componentes de salud (espacio en disco, estado del *registry*, etc.) mediante `GET /actuator/health`.

**Impacto.** Divulgación de información interna de infraestructura a actores no autorizados; facilita el reconocimiento previo a un ataque dirigido.

**Recomendación.** Cambiar `show-details` a `when-authorized` (requiere autenticación, ver hallazgo #1) o `never` en entornos sin capa de autenticación.

---

### 5.6. Hallazgo 6 — CWE-319 — Transmisión en texto claro de información sensible

**Componente:** los cuatro módulos, `docker-compose.yml`
**Severidad:** Alta
**CWE:** [CWE-319: Cleartext Transmission of Sensitive Information](https://cwe.mitre.org/data/definitions/319.html)

**Evidencia.** Ningún `application.yml` ni `application-docker.yml` del proyecto define propiedades `server.ssl.*`. Los cuatro servicios arrancan en HTTP plano (puertos 8761, 8081, 8082, 8080), y `docker-compose.yml` publica esos puertos directamente al host (líneas 47, 68, 94, 118) sin ningún componente de terminación TLS delante.

**Impacto.** Todo el tráfico entre cliente y `api-gateway`, entre `api-gateway` y los microservicios, y entre los microservicios y sus bases de datos PostgreSQL, viaja sin cifrar. Esto incluye las credenciales de base de datos del hallazgo #3 y los datos de negocio (matrículas, fechas de alquiler).

**Recomendación.** Terminar TLS en el borde (API Gateway o *reverse proxy* delante de él) con un certificado válido, y, si el modelo de amenaza lo justifica, habilitar TLS también en el tráfico interno entre servicios y hacia PostgreSQL (`sslMode=verify-full` en el driver JDBC, relevante también para el hallazgo #12).

---

### 5.7. Hallazgo 7 — CWE-915 — Asignación masiva en `VehiculoRequest.estado`

**Componente:** `vehiculos-service`
**Severidad:** Alta
**CWE:** [CWE-915: Improperly Controlled Modification of Dynamically-Determined Object Attributes](https://cwe.mitre.org/data/definitions/915.html)

**Evidencia.**

```java
// vehiculos-service/src/main/java/com/alquiler/vehiculos/web/dto/VehiculoRequest.java:27-28
@NotNull
private EstadoVehiculo estado;
```

```java
// vehiculos-service/src/main/java/com/alquiler/vehiculos/web/VehiculoController.java:57
public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest request) {
    return vehiculoService.crear(request);
}
```

El DTO de entrada vincula directamente el campo `estado` (enum `EstadoVehiculo`: `DISPONIBLE`, `ALQUILADO`, `MANTENIMIENTO`, etc.) desde el JSON del cliente, tanto en creación (`POST /api/vehiculos`) como en actualización (`PUT /api/vehiculos/{id}`), sin que exista ninguna máquina de estados que valide la transición. El contraste con el diseño de `operaciones-service` es revelador: `SolicitudRequest` (`operaciones-service/.../web/dto/SolicitudRequest.java:9-16`) es un *record* que **no** expone `estado` ni `id` — el estado de una `Solicitud` solo cambia a través de los *endpoints* dedicados `/confirmar` y `/cancelar`, que sí validan la transición (`SolicitudService.java:65-68,76-79` lanza `TransicionEstadoInvalidaException` si el estado actual no es `PENDIENTE`).

```
VehiculoRequest (cliente controla "estado")          SolicitudRequest (servidor controla "estado")
        │                                                       │
        ▼                                                       ▼
  crear()/actualizar() ── sin validación de transición    registrar() → PENDIENTE (fijo)
        │                                                       │
        ▼                                            /confirmar, /cancelar
  estado = CUALQUIERA                                  (valida estado actual antes de mutar)
```

**Impacto.** Un cliente puede crear o actualizar un vehículo directamente en estado `ALQUILADO` sin que exista ninguna solicitud de alquiler asociada, o revertir a `DISPONIBLE` un vehículo que `operaciones-service` considera actualmente alquilado, rompiendo la consistencia entre los dos microservicios (que, al ser bases de datos separadas, no tienen una transacción distribuida que lo impida).

**Recomendación.** Retirar `estado` de `VehiculoRequest` en las operaciones de escritura expuestas al cliente (usar un DTO distinto para creación sin ese campo, fijando `DISPONIBLE` por defecto en el servidor) y exponer, igual que en `operaciones-service`, *endpoints* específicos para las transiciones de estado que sí sean válidas, con su propia validación de máquina de estados en `VehiculoService`.

---

### 5.8. Hallazgo 8 — CWE-770 — Ausencia de límites de tasa (*rate limiting*)

**Componente:** `api-gateway`
**Severidad:** Media
**CWE:** [CWE-770: Allocation of Resources Without Limits or Throttling](https://cwe.mitre.org/data/definitions/770.html)

**Evidencia.** `api-gateway/src/main/resources/application.yml:9-37` define cuatro rutas Spring Cloud Gateway sin ningún filtro `RequestRateLimiter`, y el `pom.xml` del módulo (`api-gateway/pom.xml:19-40`) no incluye `spring-boot-starter-data-redis-reactive` ni ninguna otra dependencia necesaria para implementar limitación de tasa.

**Impacto.** Combinado con el hallazgo #1 (sin autenticación), el sistema no tiene ningún mecanismo que impida a un único cliente saturar los microservicios de negocio con peticiones repetidas — ni siquiera una limitación básica por IP.

**Recomendación.** Añadir el filtro `RequestRateLimiter` de Spring Cloud Gateway respaldado por Redis, o, como alternativa más ligera, un filtro *bucket4j* a nivel de *gateway*, aplicado por IP de origen (una vez resuelto el hallazgo #14, que trata la fiabilidad de esa IP).

---

### 5.9. Hallazgo 9 — CWE-778 — Registro de auditoría insuficiente

**Componente:** los cuatro módulos
**Severidad:** Media
**CWE:** [CWE-778: Insufficient Logging](https://cwe.mitre.org/data/definitions/778.html)

**Evidencia.** No existe ninguna instrucción `Logger`/`log.info`/`log.warn` en el código de aplicación de ninguno de los cuatro módulos. La única configuración de *logging* presente en todo el proyecto es `logger-level: basic` para el cliente Feign de `operaciones-service` (`operaciones-service/src/main/resources/application.yml:27`), que registra las llamadas HTTP salientes a nivel de *framework*, no eventos de negocio.

**Impacto.** Ninguna operación sensible (creación/eliminación de vehículos, confirmación o cancelación de solicitudes, ni los previsibles intentos de acceso no autorizado una vez resuelto el hallazgo #1) queda registrada. En un incidente de seguridad, no habría rastro alguno sobre qué ocurrió, cuándo ni desde dónde.

**Recomendación.** Incorporar SLF4J (ya disponible transitivamente vía Spring Boot) para registrar como mínimo: creación/modificación/eliminación de vehículos, cambios de estado de solicitudes y, tras resolver el hallazgo #1, todo intento de autenticación fallido — evitando registrar directamente entradas de usuario sin sanear, para no introducir CWE-117 (*Log Injection*) al corregir este hallazgo.

---

### 5.10. Hallazgo 10 — CWE-250 — Contenedores ejecutados como *root*

**Componente:** los cuatro `Dockerfile`
**Severidad:** Media
**CWE:** [CWE-250: Execution with Unnecessary Privileges](https://cwe.mitre.org/data/definitions/250.html)

**Evidencia.**

```dockerfile
# vehiculos-service/Dockerfile:7-17 (idéntico patrón en los otros tres Dockerfile)
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/vehiculos-service/target/vehiculos-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Ninguno de los cuatro `Dockerfile` del proyecto define una instrucción `USER`. La imagen base `eclipse-temurin:17-jre` arranca por defecto como `root`, por lo que el proceso Java de cada microservicio se ejecuta con privilegios de superusuario dentro de su contenedor.

**Impacto.** Si un atacante lograse ejecución de código dentro de cualquiera de estos contenedores (por ejemplo, a través de los hallazgos #12 o #13), lo haría con privilegios de root, ampliando el alcance de una eventual fuga del aislamiento del contenedor (*container breakout*).

**Recomendación.** Crear un usuario sin privilegios en la etapa final de cada `Dockerfile` (`RUN groupadd -r app && useradd -r -g app app`) y añadir `USER app` antes del `ENTRYPOINT`, siguiendo la recomendación estándar de *hardening* de imágenes de contenedor de OWASP y del propio *Docker Bench for Security*.

---

### 5.11. Hallazgo 11 — CWE-693 — Ausencia de cabeceras de seguridad HTTP

**Componente:** los cuatro módulos
**Severidad:** Media
**CWE:** [CWE-693: Protection Mechanism Failure](https://cwe.mitre.org/data/definitions/693.html)

**Evidencia.** Spring Security, cuando está presente en el *classpath*, añade automáticamente un conjunto de cabeceras de respuesta HTTP de protección (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`, `Cache-Control` en respuestas sensibles). Dado que ningún módulo del proyecto declara `spring-boot-starter-security` (ver hallazgo #1), ninguna de estas cabeceras se añade a las respuestas de la API, y no existe ningún `WebMvcConfigurer` ni filtro personalizado que las supla.

**Impacto.** Aumenta la superficie ante ataques del lado del cliente en cualquier interfaz que consuma esta API (por ejemplo, *clickjacking* si en el futuro se embebe una vista administrativa en un `iframe`, o *MIME sniffing* en clientes que procesen las respuestas JSON como si fueran otro tipo de contenido).

**Recomendación.** Este hallazgo se resuelve como efecto colateral directo de incorporar Spring Security para el hallazgo #1 (las cabeceras se activan por defecto al añadir la dependencia); si por alguna razón no se adoptase Spring Security, añadir manualmente un filtro Servlet que fije estas cabeceras en cada respuesta.

---

### 5.12. Hallazgo 12 — CWE-287 / CVE-2025-49146 — Bypass de *channel binding* en el driver JDBC de PostgreSQL

**Componente:** `vehiculos-service`, `operaciones-service` (driver `org.postgresql:postgresql`, versión 42.7.4 heredada del BOM de `spring-boot-starter-parent:3.3.6`)
**Severidad:** Alta (CVSS 8.2)
**CWE:** [CWE-287: Improper Authentication](https://cwe.mitre.org/data/definitions/287.html)

**Evidencia.** El `pom.xml` padre (`pom.xml:14-18`) fija `spring-boot-starter-parent` en la versión `3.3.6`, cuyo BOM gestiona el driver `org.postgresql:postgresql` en la versión `42.7.4`. Esta versión está confirmada como afectada por **CVE-2025-49146**: cuando el driver se configura con `channelBinding=required`, en las versiones 42.7.4 a 42.7.6 permite igualmente conexiones autenticadas mediante métodos que no soportan *channel binding* (password, MD5, GSS, SSPI), invalidando la protección que ese parámetro pretende ofrecer frente a ataques de intermediario (MITM). Corregido en la versión 42.7.7. Fuente: [aviso oficial de PostgreSQL JDBC](https://www.postgresql.org/about/news/postgresql-jdbc-4277-security-update-for-cve-2025-49146-3088/) / [GHSA-hq9p-pm7w-8p54](https://github.com/advisories/GHSA-hq9p-pm7w-8p54).

**Impacto.** Este hallazgo agrava directamente el hallazgo #6 (sin TLS): incluso si en el futuro se configurase `channelBinding=required` como medida de protección adicional para la conexión a PostgreSQL, la versión actual del driver no la haría efectiva frente a un atacante en posición de intermediario.

**Recomendación.** Forzar explícitamente la versión del driver a `42.7.7` o superior mediante `<dependencyManagement>` en el `pom.xml` padre (sobrescribiendo la versión del BOM de Spring Boot), y, mientras no se aplique *channel binding*, usar como mínimo `sslMode=verify-full` en la cadena de conexión JDBC.

---

### 5.13. Hallazgo 13 — CWE-94 / CVE-2025-41243 — Inyección SpEL en Spring Cloud Gateway

**Componente:** `api-gateway` (Spring Cloud Gateway Server WebFlux, versión 4.1.6, resuelta transitivamente desde `spring-cloud.version: 2023.0.4` en `pom.xml:42`)
**Severidad:** Crítica si se dan las condiciones de explotación (CVSS 10.0); **no explotable en la configuración actual del repositorio**
**CWE:** [CWE-94: Improper Control of Generation of Code ('Code Injection')](https://cwe.mitre.org/data/definitions/94.html)

**Evidencia.** `api-gateway/pom.xml:19-40` declara `spring-cloud-starter-gateway` (variante reactiva/WebFlux) y `spring-boot-starter-actuator`, las dos dependencias necesarias para que aplique CVE-2025-41243. Esta vulnerabilidad permite, a través del *endpoint* de *actuator* `gateway`, inyectar expresiones **SpEL** que modifican propiedades del `Environment` de Spring, derivando en ejecución remota de código. Afecta a Spring Cloud Gateway Server WebFlux 4.0.0–4.3.0. Fuente: [spring.io/security/cve-2025-41243](https://spring.io/security/cve-2025-41243/) / [GHSA-q2cj-h8fw-q4cc](https://github.com/advisories/GHSA-q2cj-h8fw-q4cc). Una variante relacionada de la misma familia, **CVE-2025-41253** (CWE-917, CVSS 7.5), permite exponer variables de entorno mediante el mismo vector.

Sin embargo, la condición necesaria para explotar ambas es que el *endpoint* `gateway` del *actuator* esté expuesto (`management.endpoints.web.exposure.include` debe incluir `gateway` o `*`). La configuración actual del proyecto (`api-gateway/src/main/resources/application.yml:46-50`) solo expone `health,info`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

```
Atacante                     api-gateway (:8080)
   │  POST /actuator/gateway/routes/...   │
   │  (payload SpEL en el body)           │
   ▼                                       ▼
     ── BLOQUEADO hoy: endpoint "gateway" NO expuesto ──
     ── SOLO 1 cambio de configuración lo habilitaría ──
```

**Impacto.** Hoy, **no explotable** con la configuración vigente — pero la versión de la dependencia sigue siendo vulnerable, y no hay ningún control (test automatizado, *linter* de configuración) que impida que un cambio futuro en `management.endpoints.web.exposure.include` (por ejemplo, `include: '*'` durante una sesión de depuración, un error habitual en la práctica) reintroduzca el vector de forma inmediata y con impacto máximo.

**Recomendación.** Actualizar `spring-cloud.version` a una línea parcheada (4.1.12 o superior en la serie 4.1.x, según el aviso oficial), y, como control adicional, excluir explícitamente `gateway` de `management.endpoints.web.exposure.include` (en vez de depender solo de que no esté en la lista de inclusión) o protegerlo con Spring Security una vez resuelto el hallazgo #1.

---

### 5.14. Hallazgo 14 — CWE-444 / CVE-2025-41235 — Reenvío de cabeceras `X-Forwarded-*` sin validar

**Componente:** `api-gateway` (Spring Cloud Gateway Server, versión 4.1.6)
**Severidad:** Alta (CVSS 8.6); impacto actual limitado por ausencia de lógica dependiente de IP
**CWE:** [CWE-444: Inconsistent Interpretation of HTTP Requests ('Request Smuggling')](https://cwe.mitre.org/data/definitions/444.html)

**Evidencia.** Spring Cloud Gateway Server (afectado en el rango 2.2.10–4.2.2, que incluye la versión 4.1.6 resuelta en este proyecto) reenvía por defecto las cabeceras `X-Forwarded-For` y `Forwarded` recibidas de **cualquier** origen, sin verificar que provengan de un proxy de confianza, salvo que se configure explícitamente `spring.cloud.gateway.trusted-proxies`. Ninguno de los `application.yml` de `api-gateway` define esta propiedad. Fuente: [spring.io/security/cve-2025-41235](https://spring.io/security/cve-2025-41235/) / [GHSA-6j2q-c73v-97c5](https://github.com/advisories/GHSA-6j2q-c73v-97c5).

**Impacto.** Cualquier cliente externo puede falsificar su IP de origen aparente ante los microservicios internos. En el estado actual del proyecto el impacto es latente, porque ningún componente de la aplicación toma decisiones basadas en la IP del cliente — pero es una precondición directa que **anularía** cualquier control de `rate limiting` por IP que se implemente para resolver el hallazgo #8, si no se corrige antes.

**Recomendación.** Actualizar a Spring Cloud Gateway 4.1.8 o superior, y configurar `spring.cloud.gateway.trusted-proxies` con la expresión regular que identifique únicamente los proxies/balanceadores legítimos que preceden al *gateway* en el despliegue real.

---

### 5.15. Hallazgo 15 — CWE-377 / CVE-2026-40973 — Directorio temporal predecible en Spring Boot

**Componente:** los cuatro módulos (`spring-boot-starter-parent:3.3.6`)
**Severidad:** Baja–Media (requiere acceso local al host o contenedor; CVSS ~7.0 según el aviso, mitigado aquí por el modelo de despliegue)
**CWE:** [CWE-377: Insecure Temporary File](https://cwe.mitre.org/data/definitions/377.html)

**Evidencia.** El `pom.xml` padre fija `spring-boot-starter-parent` en la versión `3.3.6` (`pom.xml:16`), incluida en el rango afectado 3.3.0–3.3.18 de **CVE-2026-40973**: Spring Boot acepta el directorio temporal usado por `ApplicationTemp` sin verificar su propiedad (*ownership*), permitiendo a un atacante con acceso local al mismo host tomar control de ese directorio mediante una condición de carrera. Corregido en 3.3.19. Fuente: [spring.io/security/cve-2026-40973](https://spring.io/security/cve-2026-40973/) / [GHSA-wwpq-f5c3-7hvx](https://github.com/advisories/GHSA-wwpq-f5c3-7hvx).

**Impacto.** El vector de explotación exige que el atacante ya tenga ejecución de código en el mismo host o contenedor — en el despliegue actual (un proceso Java por contenedor, sin sesiones HTTP persistentes ya que la API es completamente *stateless*) el escenario de mayor impacto descrito en el aviso (secuestro de sesión vía `server.servlet.session.persistent=true`) no aplica, porque esa propiedad no está configurada en ningún `application.yml` del proyecto. El riesgo residual es la ejecución de código como el usuario del proceso — agravado, no obstante, por el hallazgo #10 (contenedores como *root*).

**Recomendación.** Actualizar a Spring Boot 3.3.19 o superior en el `pom.xml` padre; es una actualización de bajo riesgo de regresión al tratarse de una versión *patch* dentro de la misma línea 3.3.x.

---

## 6. Conclusiones

La auditoría de código estático del sistema de alquiler de vehículos permite extraer tres conclusiones principales:

**Primera**, el escaneo automatizado por sí solo (Semgrep con reglas de patrón *community*, 0 hallazgos sobre 161 reglas) habría sido insuficiente para cumplir el objetivo de la actividad. Los antipatrones de codificación clásicos que este tipo de herramientas detecta bien (inyección SQL, XSS reflejado, deserialización insegura) están efectivamente ausentes del código de negocio, que usa de forma consistente APIs parametrizadas (JPA Criteria, JPQL con *named parameters*) y no maneja vistas *server-side*. El riesgo real de esta aplicación no reside en errores de codificación puntuales, sino en **decisiones arquitectónicas ausentes** —falta de una capa de autenticación completa— y en **configuración por defecto insegura** heredada de plantillas de ejemplo (credenciales, TLS, cabeceras), que solo aparecen mediante revisión dirigida del código y la configuración, y en **dependencias desactualizadas con CVE publicados**, que solo aparecen mediante análisis de composición de software (SCA). Una auditoría de seguridad de código completa necesita combinar los tres enfoques.

**Segunda**, el nivel de riesgo agregado para la organización es **alto**. La combinación de la ausencia total de autenticación (hallazgo #1) con la ausencia de *rate limiting* (#8), TLS (#6) y registro de auditoría (#9) significa que, en su estado actual, la aplicación no tiene ningún control compensatorio que limite el impacto de un ataque una vez que un actor accede a la red donde se despliega. Ninguno de los 15 hallazgos requiere, individualmente, una reescritura mayor del sistema: son en su mayoría adiciones de configuración (cabeceras, TLS, `trusted-proxies`, exclusión de *endpoints*) o de una dependencia estándar del ecosistema Spring (`spring-boot-starter-security`), lo que hace que la relación esfuerzo de remediación / reducción de riesgo sea muy favorable.

**Tercera**, cuatro de los 15 hallazgos (#12–#15) no habrían sido detectables sin verificar activamente, contra fuentes oficiales, las versiones exactas de las dependencias declaradas en los `pom.xml` frente a los avisos de seguridad publicados por Spring y por el proyecto PostgreSQL JDBC — lo que confirma que la gestión de dependencias (SCA) es hoy una superficie de riesgo tan relevante como el propio código fuente escrito por el equipo de desarrollo, especialmente en un *stack* que, como Spring Boot/Spring Cloud, recibe avisos de seguridad con frecuencia.

## 7. Bibliografía

- MITRE. *Common Weakness Enumeration (CWE)*. https://cwe.mitre.org/
- Semgrep, Inc. *Semgrep Documentation*. https://semgrep.dev/docs/
- Spring.io. *Spring Security Advisories*. https://spring.io/security
  - CVE-2025-49146 — PostgreSQL JDBC 42.7.7 Security update. https://www.postgresql.org/about/news/postgresql-jdbc-4277-security-update-for-cve-2025-49146-3088/
  - CVE-2025-41243 — Spring Expression Language property modification using Spring Cloud Gateway Server WebFlux. https://spring.io/security/cve-2025-41243/
  - CVE-2025-41235 — Spring Cloud Gateway Server Forwards Headers from Untrusted Proxies. https://spring.io/security/cve-2025-41235/
  - CVE-2026-40973 — Predictable temp directory accepted without ownership verification. https://spring.io/security/cve-2026-40973/
- GitHub Security Advisory Database. https://github.com/advisories
  - GHSA-hq9p-pm7w-8p54, GHSA-q2cj-h8fw-q4cc, GHSA-6j2q-c73v-97c5, GHSA-wwpq-f5c3-7hvx
- OWASP Foundation. *OWASP Top Ten 2021*. https://owasp.org/Top10/
- OWASP Foundation. *Docker Security Cheat Sheet*. https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html
- VV. AA. Documentación oficial de Spring Boot 3.3 y Spring Cloud 2023.0. https://docs.spring.io/
