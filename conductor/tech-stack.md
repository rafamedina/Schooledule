# Tech Stack

## Backend
- **Language:** Java 21 (with Lombok for boilerplate reduction on entities)
- **Framework:** Spring Boot 3.3.5
- **Security:** Spring Security 6 (using RBAC)
- **Data Access:** Spring Data JPA + Hibernate 6
- **Validation:** Spring Boot Starter Validation
- **Session Management:** Spring Session for JDBC
- **DTO Mapping:** MapStruct 1.5.5.Final (compile-time, `componentModel = "spring"`). DTOs are Java `record` types.

## Frontend
- **Templating Engine:** Thymeleaf (with Spring Security 6 integration)
- **CSS Framework:** Bootstrap 5
- **JavaScript:** Native JavaScript (for minimal interactivity and form handling)

## Database
- **Engine:** PostgreSQL 16
- **Features:** JSONB for hybrid evaluation data, PL/pgSQL triggers for auditing, partial indexing.
- **ORM Extensions:** Hypersistence Utils (for Hibernate JSON handling)

## Infrastructure
- **Containerization:** Docker Compose
- **Build Tool:** Maven 3

## Testing
- **Framework:** JUnit 5 + Mockito
- **Tools:** Spring Security Test support, AssertJ (standard in Spring Boot test starter), H2 Database (for in-memory integration testing)

## Variables de entorno requeridas en producción

| Variable | Descripción | Fallback en local |
|----------|-------------|-------------------|
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `root123` (solo local) |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring activo | — (dev si no se especifica) |

> En producción, `DB_PASSWORD` **no tiene fallback seguro** — si no está definida la app arrancará con `root123`. Siempre definir `SPRING_PROFILES_ACTIVE=prod` para activar `application-prod.properties` (cookie segura, sin show-sql, logs WARN).

**Docker Compose** (`infraestructura/docker-compose.yml`): el servicio `app` sobreescribe las credenciales vía variables de entorno Spring (`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) que Spring Boot mapea automáticamente — no se usan `DB_USERNAME`/`DB_PASSWORD` en contenedor.
