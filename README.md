# E-Commerce Backend (Taller 1 de Java Avanzado)

## 1. Descripción del Proyecto
Este proyecto es una API REST para el backend de un sistema de comercio electrónico desarrollado como parte del taller de "Java Backend Avanzado". Su objetivo principal es administrar usuarios, productos, inventario y órdenes de compra, garantizando el soporte para solicitudes concurrentes, protección de información sensible y una arquitectura de software limpia y sostenible.

## 2. Tecnologías y Herramientas Utilizadas
- **Java 21+** (Uso de características modernas: `records` para DTOs, `Streams` y `Optional`)
- **Spring Boot 3** (Spring Web, Spring Data JPA, Spring Security)
- **PostgreSQL** (Base de datos relacional)
- **MapStruct** (Mapeo entre Entidades, Modelos y DTOs)
- **JWT (JSON Web Tokens)** (Autenticación y autorización)
- **Docker / Docker Compose** (Contenerización de la base de datos)
- **JUnit 5 / Mockito** (Pruebas unitarias, de integración y simulación de concurrencia)
- **OpenAPI / Swagger** (Documentación de la API REST)

## 3. Decisiones Técnicas y Arquitectura
El proyecto sigue una **Arquitectura en Capas** con separación estricta de responsabilidades (Domain, Persistence, Service, Controller, Utils) para mantener un bajo acoplamiento y justificar sus responsabilidades.

- **Concurrencia (Regla de Negocio Central):** Para evitar la sobreventa cuando múltiples clientes intentan comprar el mismo producto simultáneamente, se implementó **Optimistic Locking** (Control de Concurrencia Optimista) usando la anotación `@Version` en la entidad `Product`. Si ocurre una colisión, se lanza una excepción que se traduce en un error HTTP 409 Conflict. Esto se demuestra mediante pruebas de concurrencia en la suite de testing.
- **Java Moderno:** Se utilizan `records` inmutables para la transferencia de datos (DTOs), API de `Streams` para el procesamiento de colecciones reales, y `Optional` para evitar `NullPointerException` en las consultas.
- **Persistencia y Transacciones:** Uso de Spring Data JPA con control transaccional estricto en la creación de órdenes. Se implementó **Eliminación Lógica (Soft Delete)** para los productos (`active = false`).
- **Seguridad y Auditoría:** 
  - Autenticación sin estado (Stateless) con JWT.
  - Autorización basada en roles (`ADMIN` y `CLIENTE`).
  - Protección de contraseñas mediante hashing seguro.
  - Registro de auditoría (intentos de inicio de sesión, accesos denegados, creación de órdenes y productos) gestionado de forma asíncrona.
- **Diseño API REST:** Endpoints semánticos, uso correcto de códigos de estado HTTP, validación de datos de entrada y manejo global de excepciones mediante `@RestControllerAdvice`.

## 4. Instrucciones de Ejecución

1. **Levantar la Base de Datos:**
   Asegúrate de tener Docker instalado y ejecutándose.
   ```bash
   docker-compose up -d
   ```

2. **Ejecutar la Aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(En Windows CMD/PowerShell, usa `mvnw.cmd spring-boot:run`)*

## 5. Credenciales de Prueba
*Nota: Asegúrate de ejecutar los scripts de inicialización de datos (si aplican) o registrar estos usuarios a través del endpoint correspondiente para realizar las pruebas de los roles.*
- **Administrador:** `admin@example.com` / `admin123` (Rol: ADMIN)
- **Cliente:** Para que la prueba sea efectiva, registra un nuevo cliente a través del endpoint de registro.

## 6. Testing y Evidencia de Concurrencia

Las pruebas incluyen la verificación del comportamiento transaccional y la prevención de sobreventa concurrente solicitada en el taller.

NOTA: Tuvimos que eliminar la evidencia de las pruebas de concurrencia debido a limitaciones en el entorno de ejecución, 
pero se implementaron pruebas unitarias y de integración que simulan múltiples hilos intentando comprar el mismo 
producto para validar la lógica de bloqueo optimista.
