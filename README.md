# Microservicios de Comercio Electrónico (Arquitectura Guiada por Eventos)

## 1. Descripción
Este proyecto implementa un sistema de comercio electrónico modular y guiado por eventos usando Java 21 y Spring Boot 4. Presenta una interacción basada en coreografía entre la gestión de pedidos y el procesamiento de pagos, lo que garantiza un alto desacoplamiento y escalabilidad.

## 2. Arquitectura y Guardrails
El sistema sigue límites arquitectónicos estrictos para mantener la independencia de los servicios:
- **Integración de Payments solo por eventos:** El servicio `payments-service` y la aplicación `commerce-app` se comunican exclusivamente mediante eventos de RabbitMQ.
- **Sin HTTP directo:** No existen llamadas HTTP sincrónicas desde `commerce-app` hacia `payments-service`.
- **Sin base de datos compartida:** Cada servicio posee su propia base de datos. Commerce usa PostgreSQL para pedidos e inventario, mientras que Payments usa un esquema independiente para el historial de pagos. No existe una **base de datos compartida** entre estos módulos.
- **Umbral de pago:** Los pagos se simulan de forma determinista en función de un **umbral de pago**.
- **Coreografía:** Los procesos de negocio están guiados por eventos, sin un orquestador central.

## 3. Estructura del Proyecto
- `event-contracts`: Contiene los registros de eventos compartidos y la `EventTopology` centralizada.
- `commerce-app`: Gestiona usuarios, productos, inventario y el checkout de pedidos.
- `payments-service`: Procesa solicitudes de pago y mantiene un historial de auditoría de los intentos.

## 4. Configuración Local y Dependencias
El proyecto requiere PostgreSQL y RabbitMQ. Se proporciona un archivo Docker Compose para una configuración rápida.

### Preparar variables de entorno (.env)
Antes de levantar infraestructura o ejecutar los servicios, crea tu archivo `.env` a partir de `.env.example`:

```bash
cp .env.example .env
```

En Windows PowerShell puedes usar:

```powershell
Copy-Item .env.example .env
```

Tanto `commerce-app` como `payments-service` importan `.env` de forma opcional mediante `spring.config.import`, por lo que las propiedades se pueden resolver directamente desde ese archivo.

### Iniciar la infraestructura
```bash
docker-compose up -d
```
Este comando inicia:
- **PostgreSQL:** Puerto 5432 (Base de datos: `taller-java-db`).
- **RabbitMQ:** Puerto 5672 (Interfaz de administración: `http://localhost:15672`).

### Configuración de los servicios
Variables de entorno y propiedades clave para el **umbral de pago**:
- `APP_PAYMENTS_MAX_SUCCESS_AMOUNT`: Variable de entorno.
- `app.payments.max-success-amount`: Propiedad de Spring (valor por defecto: `1000.00`).
- `COMMERCE_DATASOURCE_DB`: Nombre de base de datos para `commerce-app` (valor por defecto: `taller-java-db`).
- `PAYMENTS_DATASOURCE_URL`: URL JDBC para `payments-service` (por defecto usa H2 local).
- `SPRING_RABBITMQ_HOST/PORT/USERNAME/PASSWORD`: Datos de conexión para el broker de mensajes.
- `SPRING_DATASOURCE_USERNAME/PASSWORD`: Credenciales compartidas para PostgreSQL (Docker) y datasources.
- `JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION`: Configuración JWT de `commerce-app`.
- `APP_BOOTSTRAP_FIRST_NAME/LAST_NAME/EMAIL/PASSWORD/PHONE`: Datos del usuario administrador bootstrap en `commerce-app`.

## 5. Topología de Eventos
Todos los eventos fluyen a través del Topic Exchange `commerce.events`.

### Exchange
- `commerce.events`

### Routing Keys
- `order.payment.requested.v1`
- `payment.succeeded.v1`
- `payment.failed.v1`

### Colas y DLQs
- `payments.payment-requests` (DLQ: `payments.payment-requests.dlq`)
- `commerce.payment-results` (DLQ: `commerce.payment-results.dlq`)

## 6. Ejecución y Verificación

### Verificación canónica
Ejecuta lo siguiente desde el directorio raíz para verificar todo el proyecto:
```bash
mvn clean verify
```

### Ejecución de pruebas
Las pruebas que usan RabbitMQ y Testcontainers requieren un daemon de Docker funcional. Si Docker no está disponible, estas pruebas se omitirán automáticamente.

### Verificación dirigida por módulo
```bash
# Verificar contratos de eventos
mvn -pl event-contracts -am verify

# Verificar la aplicación de commerce
mvn -pl commerce-app -am verify

# Verificar el servicio de pagos
mvn -pl payments-service -am verify

# Ejecutar pruebas de integración
mvn -pl commerce-app -am verify -Dtest=PaymentChoreographyIntegrationTest

```

### Ejecutar el proyecto completo
Desde Windows PowerShell, puedes levantar infraestructura local, instalar los módulos y abrir ambos servicios con:

```powershell
.\run-project.ps1
```

En macOS o Linux, usa el script de shell:

```bash
chmod +x ./run-project.sh
./run-project.sh
```

El script hace lo siguiente:
- Crea `.env` desde `.env.example` si todavía no existe.
- Ejecuta `docker compose up -d postgres rabbitmq`.
- Ejecuta el Maven wrapper (`mvnw.cmd` en Windows o `./mvnw` en macOS/Linux) con `-DskipTests install` para instalar los módulos locales. Si no existe el wrapper, usa `mvn`.
- Inicia `commerce-app` en `http://localhost:8080`.
- Inicia `payments-service`, que actúa como worker de RabbitMQ.

En Windows, cada servicio se abre en una ventana de PowerShell independiente. En macOS/Linux, ambos servicios corren como procesos de fondo en la misma terminal y se detienen con `Ctrl+C`.

Opciones útiles:

```powershell
# No levantar Docker Compose
.\run-project.ps1 -SkipInfra

# No reinstalar módulos Maven
.\run-project.ps1 -SkipBuild

# Cambiar el puerto HTTP de commerce-app
.\run-project.ps1 -CommercePort 8082

# Mostrar lo que ejecutaría sin iniciar contenedores ni servicios
.\run-project.ps1 -DryRun
```

Opciones equivalentes en macOS/Linux:

```bash
# No levantar Docker Compose
./run-project.sh --skip-infra

# No reinstalar módulos Maven
./run-project.sh --skip-build

# Cambiar el puerto HTTP de commerce-app
./run-project.sh --commerce-port 8082

# Mostrar lo que ejecutaría sin iniciar contenedores ni servicios
./run-project.sh --dry-run
```

> Nota: el proyecto raíz `taller_1_java` es un agregador Maven con `packaging=pom`, por lo que `mvn spring-boot:run` desde la raíz no es un comando válido para arrancar la aplicación. Si prefieres ejecutar servicios manualmente, apunta al POM del módulo ejecutable:

```powershell
# Iniciar Commerce App
$env:SERVER_PORT = "8080"
.\mvnw.cmd -f commerce-app/pom.xml spring-boot:run

# Iniciar Payments Service
.\mvnw.cmd -f payments-service/pom.xml spring-boot:run
```

En macOS/Linux:

```bash
# Iniciar Commerce App
SERVER_PORT=8080 ./mvnw -f commerce-app/pom.xml spring-boot:run

# Iniciar Payments Service
./mvnw -f payments-service/pom.xml spring-boot:run
```

## 7. Limitaciones Actuales
Esta iteración se centra en el flujo principal guiado por eventos y excluye intencionalmente:
- Patrones outbox de nivel productivo.
- Orquestación de saga o gestores centralizados de procesos.
- Reembolsos e integraciones directas con proveedores de pago.
- Seguridad avanzada de microservicios (la autenticación está enfocada en patrones monolíticos).

## 8. Swagger / OpenAPI (commerce-app)
La documentación interactiva de la API está habilitada en `commerce-app` mediante Springdoc.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Esquema de seguridad documentado: `Bearer JWT`

> Nota: según `SecurityConfig`, las rutas de Swagger (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`) están permitidas sin autenticación.

## 9. Tecnologías Utilizadas
- `Java 21`
- `Spring Boot 4` (Web MVC, Security, Data JPA, Validation, AMQP)
- `RabbitMQ` (mensajería asíncrona entre servicios)
- `PostgreSQL` (persistencia principal en commerce)
- `H2` (persistencia local/runtime en payments-service)
- `Maven` multi-módulo
- `Springdoc OpenAPI` (documentación API en commerce-app)
- `JUnit 5` + `Spring Boot Test` + `Testcontainers` (pruebas)
- `Docker Compose` (infraestructura local)

## 10. Decisiones Técnicas Clave
- **Arquitectura por módulos:** separación en `event-contracts`, `commerce-app` y `payments-service` para desacoplar contratos, dominio de comercio y procesamiento de pagos.
- **Comunicación por eventos:** `commerce-app` no llama por HTTP a `payments-service`; la integración se hace por RabbitMQ con coreografía.
- **Contratos versionados:** uso de eventos `*.v1` y topología centralizada en `EventTopology`.
- **Idempotencia:** deduplicación de resultados de pago para tolerar reentregas del broker.
- **Checkout asíncrono:** endpoint de checkout devuelve `202 Accepted` y continúa por flujo eventual.
- **Seguridad stateless en commerce:** JWT + RBAC para endpoints REST.

## 11. Credenciales y Variables de Entorno (Desarrollo)
La plantilla completa de variables está en `.env.example`.

### Usuario bootstrap de `commerce-app`
Con la configuración por defecto (o variables `APP_BOOTSTRAP_*`):
- Email: `admin@test.com`
- Password: `Admin123!`

### RabbitMQ (valores por defecto)
- Usuario: `guest`
- Password: `guest`
- UI: `http://localhost:15672`

### Base de datos (según entorno)
- `docker-compose.yml` usa `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` para PostgreSQL.
- `commerce-app` usa `COMMERCE_DATASOURCE_DB` para el nombre de base de datos (default: `taller-java-db`).
- `payments-service` usa `PAYMENTS_DATASOURCE_URL` (default H2 local en archivo).

> Recomendación: para desarrollo compartido, define explícitamente las variables de entorno y evita depender de fallbacks en texto plano.
