## Memory
You have access to Engram persistent memory via MCP tools (mem_save, mem_search, mem_session_summary, etc.).
- Save proactively after significant work — don't wait to be asked.
- After any compaction or context reset, call `mem_context` to recover session state before continuing.

# GEMINI.md (Schooledule)

Multi-Site Academic Management Platform (ERP) built with Java 21 and Spring Boot 3.3.5.

## Reference Index (When you encounter problems)

| Trigger scenario                | Document                           | Core content                                      |
| ------------------------------- | ---------------------------------- | ------------------------------------------------- |
| Understanding product goals     | `conductor/product.md`             | Vision, target users, and core features.          |
| Tech stack or dependency issues | `conductor/tech-stack.md`          | Versions, libraries, and architectural decisions. |
| CI/CD & Promotion Flow          | `conductor/workflow.md`            | Detailed "Purge & Promote" and PR process.        |
| Unsure about task workflow      | `conductor/workflow.md`            | TDD, Git guidelines, and Quality Gates.           |
| Starting a new feature/track    | `conductor/tracks.md`              | Implementation tracks and their status.           |
| Database schema or migrations   | `src/main/resources/db/migration/` | Flyway migration scripts.                         |

## Essential Commands

| Action     | Command                                    |
| ---------- | ------------------------------------------ |
| Run App    | `./mvnw spring-boot:run`                   |
| Run Tests  | `./mvnw test`                              |
| Build      | `./mvnw clean install`                     |
| Start DB   | `cd infraestructura; docker-compose up -d` |
| Pre-commit | `pre-commit run --all-files`               |

## CI/CD & Branching Strategy (Clean Registry)

1. **Branch `dev`**: Desarrollo activo. Contiene herramientas de IA y documentación técnica.
2. **Branch `pre-prod`**: Rama purgada automáticamente tras el éxito de los tests en `dev`.
3. **Branch `main`**: Producción. Solo accesible vía PR manual desde `pre-prod`.

**Purge Policy**: En `pre-prod/main` se eliminan `conductor/`, `.agents/`, `GEMINI.md`, etc. Solo queda el código fuente y la infraestructura mínima.

## Repository Structure

- `src/main/java/com/tfg/schooledule/`: Application source code.
  - `domain/`: Entities, DTOs, and Enums.
  - `infrastructure/`: Config, Controllers, Repositories, Services, and Security.
- `src/main/resources/`:
  - `templates/`: Thymeleaf templates.
  - `static/`: CSS and JavaScript.
  - `db/migration/`: Flyway SQL migrations.
- `src/test/`: Unit and integration tests.
- `conductor/`: Project management and deep technical documentation.

## Specialized AI Skills (Available via `activate_skill`)

| Skill                                | Definición de Uso                                                 | Link                                            |
| :----------------------------------- | :---------------------------------------------------------------- | :---------------------------------------------- |
| **java-expert**                      | Desarrollo experto en Java 21+, Maven y patrones empresariales.   | `.../java-expert/SKILL.md`                      |
| **java-springboot**                  | Mejores prácticas y configuración para Spring Boot 3.3.           | `.../java-springboot/SKILL.md`                  |
| **java-junit**                       | Pruebas unitarias exhaustivas con JUnit 5 y Mockito.              | `.../java-junit/SKILL.md`                       |
| **springboot-tdd**                   | Ciclo de vida completo TDD (Red-Green-Refactor) para Spring Boot. | `.../springboot-tdd/SKILL.md`                   |
| **supabase-postgres-best-practices** | Optimización de consultas y diseño de esquemas en Postgres.       | `.../supabase-postgres-best-practices/SKILL.md` |
| **frontend-design**                  | Creación de interfaces web modernas y pulidas (Vanilla/React).    | `.../frontend-design/SKILL.md`                  |
| **cicd-expert**                      | Diseño y depuración de pipelines (GitHub Actions).                | `.../cicd-expert/SKILL.md`                      |
| **multi-stage-dockerfile**           | Creación de Dockerfiles multi-etapa optimizados.                  | `.../multi-stage-dockerfile/SKILL.md`           |
| **docker-compose-orchestration**     | Orquestación y despliegue con Docker Compose.                     | `.../docker-compose-orchestration/SKILL.md`     |
| **git-commit**                       | Gestión inteligente de commits y mensajes convencionales.         | `.../git-commit/SKILL.md`                       |
| **agents-md-creator**                | Mantenimiento de documentación contextual para IA.                | `.../agents-md-creator/SKILL.md`                |
| **find-docs**                        | Recuperación de documentación técnica actualizada.                | `.../find-docs/SKILL.md`                        |

## Iron Rules (AI Must Follow)

1. **The Plan is the Source of Truth:** Always update and follow the `plan.md` within the active track in `conductor/tracks/`.
2. **Test-Driven Development (TDD):** Write failing tests _before_ implementation. Aim for >80% coverage.
3. **PowerShell 7 Environment:** Always use PowerShell 7 compatible syntax. Avoid Linux-specific commands (like `export`, `rm -rf` without caution, etc.).
4. **Non-Interactive Commands:** Use `CI=true` and avoid watch modes or interactive prompts.
5. **Git Protocol:** Attach task summaries to commits using `git notes` as defined in `conductor/workflow.md`.
6. **Multi-Site Isolation:** Always respect `centro_id` for data isolation between different educational centers.

## Before Modifying Code

| Area         | Read this first                                               | Key Gotchas                                                   |
| ------------ | ------------------------------------------------------------- | ------------------------------------------------------------- |
| **Database** | `conductor/tech-stack.md`                                     | Uses JSONB for evaluations and PL/pgSQL for auditing.         |
| **Security** | `src/main/java/.../infrastructure/config/SecurityConfig.java` | RBAC with email-based login.                                  |
| **Workflow** | `conductor/workflow.md`                                       | Strict task lifecycle: Red -> Green -> Refactor -> Git Notes. |

---

## Information Recording Principles (Agents Must Read)

This document uses **progressive disclosure** to optimize agent effectiveness.

### Level 1 (This file) contains only:

- Core commands and navigation.
- Iron rules and critical prohibitions.
- Reference index to deeper documentation.

### Level 2 (conductor/ directory) contains:

- Detailed SOPs and workflows.
- Historical design decisions.
- Complete technology specifications.

### When recording information:

1. **Assess frequency:** High frequency -> Level 1, otherwise Level 2.
2. **Level 1 references to Level 2 must include:** Trigger condition and content summary.
3. **Never:** Bloat Level 1 with detailed flows or redundant information.
