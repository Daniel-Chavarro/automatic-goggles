# automatic-goggles
Taller 1 de Java Avanzado - Backend E-commerce

## Tecnologías
- Java 21, Spring Boot 4.0.5
- JPA/Hibernate, PostgreSQL
- JWT con Refresh Tokens
- MapStruct, OpenAPI (Swagger)
- Specifications (filtros dinámicos)
- Docker Compose (PostgreSQL)

## Ejecución

### Requisitos
- Docker Desktop
- Maven

### Levantar servicios
```bash
docker-compose up -d
./mvnw spring-boot:run
```
Puerto: `8080`

### Datos de prueba
Al iniciar se generan ~1000 users, 500 products, 3000 orders automáticamnete desde `data.sql`.

**Para desactivar** carga automática: cambiar en `application.yaml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # (create/truncate cada vez que inicia)
```

## Credenciales

| Rol | Credenciales |
|-----|-------------|
| Admin | `admin@test.com` / `Admin123!` |
| Cliente | Registro vía `/api/v1/auth/register` |

## Decisiones Técnicas
- **Arquitectura**: Capas limpias (Persistence → Domain → Service → Controller)
- **Domain Puro**: Modelos sin campos de auditoría
- **Concurrencia**: Optimistic Locking (`@Version`) para Stock
- **Soft Deletes**: Campo `active` en entidades
- **Filtros**: Specifications JPA para consultas dinámicas
- **Mapeo**: MapStruct (Entity ↔ Model ↔ DTO)
- **Auditoría**: EventLog para intentos de login y accesos no autorizados

## Testing
Actualmente, el testing fue eliminado dado por inconvenientes durante el desarrollo debido al rollback de la 
característica de seguridad. Sin embargo, se recomienda implementar pruebas unitarias y de integración utilizando 
JUnit y Mockito para asegurar la calidad del código y la funcionalidad de la aplicación.