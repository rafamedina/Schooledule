# Implementation Plan — Teacher Dashboard (`implement_teacher_dashboard_20260418`)

> **READ THIS FIRST (AI operator):** This plan is deterministic. Execute phases top-to-bottom, tasks top-to-bottom. Each task lists EXACT file paths, EXACT class / method signatures, and the TDD test you must write BEFORE the implementation (Red → Green → Refactor, per `conductor/workflow.md`). Do not skip phases. Do not invent new files outside those listed. Update this file in-place per the workflow (`[ ]` → `[~]` → `[x] <sha7>`).
>
> **Iron rules (CLAUDE.md):** PowerShell 7 syntax for commands · `centro_id` isolation in every query · TDD first · `git notes` attached after each commit.
>
> **Frontend aesthetic (required):** use the `frontend-design` skill. Target aesthetic: **editorial/academic** — serif display (Fraunces or Playfair Display) + humanist sans body (Inter-alt like *Geist* or *General Sans*), warm paper neutral (#F7F2EA) with deep accent (#2E3A5C) and single hot accent (#C24E3D). Asymmetric grid, strong oldstyle numerals for grades, generous whitespace. Bootstrap 5 kept as reset + grid only — override aggressively via CSS variables. DO NOT ship a default Bootstrap look.

---

## Domain model quick reference (DO NOT change during this track)

```
Usuario (profesor)  ──ManyToMany──▶  Centro          via `profesores_sedes(usuario_id, centro_id)`
Imparticion(id, modulo_id, grupo_id, profesor_id, centro_id, configuracion_evaluacion)
Matricula(id, alumno_id, imparticion_id, centro_id, estado estado_matricula)
PeriodoEvaluacion(id, imparticion_id, nombre, peso NUMERIC(5,2), cerrado BOOLEAN)
ItemEvaluable(id, imparticion_id, periodo_evaluacion_id, nombre, fecha, tipo)
Calificacion(id, matricula_id, item_evaluable_id, valor NUMERIC(5,2), comentario)
   UNIQUE(matricula_id, item_evaluable_id)
   trigger_auditoria_notas writes auditoria_notas on UPDATE where valor changed
```

Teacher's asignaturas for a centro = `SELECT i FROM imparticiones i WHERE i.profesor_id = :uid AND i.centro_id = :cid`.

---

## Phase 0 — Plumbing & test fixtures [checkpoint: ]

### Task 0.1 — Add `findById` & ownership queries on repositories
- [x] Edit `src/main/java/com/tfg/schooledule/infrastructure/repository/` — add the following repositories / methods:
  - Create `ImparticionRepository extends JpaRepository<Imparticion, Integer>` with:
    - `List<Imparticion> findByProfesorIdAndCentroId(Integer profesorId, Integer centroId);`
    - `boolean existsByIdAndProfesorId(Integer id, Integer profesorId);`
  - Create `CentroRepository extends JpaRepository<Centro, Integer>` (no custom methods required — teacher's centros are read via `Usuario.getCentros()` which is already mapped).
  - Create `ItemEvaluableRepository extends JpaRepository<ItemEvaluable, Integer>` with:
    - `List<ItemEvaluable> findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(Integer imparticionId);`
  - Extend `MatriculaRepository` with:
    - `List<Matricula> findByImparticionIdAndEstado(Integer imparticionId, EstadoMatricula estado);`
    - `Optional<Matricula> findByIdAndImparticionProfesorId(Integer id, Integer profesorId);`
  - Extend `CalificacionRepository` with:
    - `Optional<Calificacion> findByMatriculaIdAndItemEvaluableId(Integer mId, Integer itemId);`
    - `List<Calificacion> findByMatriculaId(Integer matriculaId);`

**Red test** (`src/test/java/.../infrastructure/repository/ImparticionRepositoryTest.java`, `@DataJpaTest`):
```
given: seed 2 profesores, 2 centros, 3 imparticiones (1 profe1@centro1, 1 profe1@centro2, 1 profe2@centro1)
when : findByProfesorIdAndCentroId(profe1.id, centro1.id)
then : list size == 1 AND the returned imparticion.centro == centro1
and  : existsByIdAndProfesorId(profe1Impart.id, profe2.id) == false
```
Mirror the same "isolation" assertion style for `MatriculaRepository.findByImparticionIdAndEstado`.

### Task 0.2 — Seed data for dev / tests
- [x] Extend `V1__Initial_Schema.sql` (**DO NOT create V2 for seed** unless the track promotes to pre-prod — keep all seed edits inside V1 for this dev iteration) with:
  - 1 extra centro (id=2 `"CIFP Norte"`).
  - Link `profe1` (usuario_id=2) to centros {1,2} and `profe_alumno` (id=4) to centro {1}.
  - 2 `modulos`, 1 `curso_academico`, 2 `grupos`, 3 `imparticiones` (profe1 teaches 2 imparticiones across 2 centros; profe_alumno teaches 1 in centro 1).
  - 3 `matriculas` (one per imparticion for `alumno1`), 2 `periodos_evaluacion` per imparticion, 3 `items_evaluables` per imparticion, and 2 `calificaciones` pre-populated on the first imparticion.
  - Re-`setval` every sequence at the end of the block.

> **Why:** gives both E2E smoke and a meaningful modal. Keep values boring & deterministic — tests will assert on them.

### Task 0.3 — `@PreAuthorize` + method security
- [x] Add `@EnableMethodSecurity` on `SecurityConfig` if not already present, so that `@PreAuthorize("hasRole('PROFESOR')")` works at the controller method level (belt-and-braces on top of the URL matcher).

---

## Phase 1 — Backend: DTOs, Mappers, Service [checkpoint: ]

### Task 1.1 — DTOs (records, per project convention)
Create each file under `src/main/java/com/tfg/schooledule/domain/dto/`:

- [x] `TeacherCenterDTO.java`
  ```java
  public record TeacherCenterDTO(Integer id, String nombre, String ubicacion, long imparticionesCount) {}
  ```
- [x] `TeacherSubjectDTO.java`
  ```java
  public record TeacherSubjectDTO(
      Integer imparticionId,
      String moduloCodigo,
      String moduloNombre,
      String grupoNombre,
      String cursoAcademicoNombre,
      long alumnosCount) {}
  ```
- [x] `TeacherStudentRowDTO.java`
  ```java
  public record TeacherStudentRowDTO(
      Integer matriculaId,
      Integer alumnoId,
      String nombreCompleto,
      String email,
      Boolean esRepetidor) {}
  ```
- [x] `TeacherGradeItemDTO.java`
  ```java
  public record TeacherGradeItemDTO(
      Integer itemEvaluableId,
      String itemNombre,
      String tipoActividad,
      LocalDate fecha,
      BigDecimal valor,        // nullable — null = not yet graded
      String comentario,
      Integer calificacionId)  // nullable — null = row not yet created
  {}
  ```
- [x] `TeacherPeriodoGradesDTO.java`
  ```java
  public record TeacherPeriodoGradesDTO(
      Integer periodoId,
      String periodoNombre,
      BigDecimal peso,
      boolean cerrado,
      List<TeacherGradeItemDTO> items,
      BigDecimal media)        // server-computed, null if no grades
  {}
  ```
- [x] `TeacherStudentGradesDTO.java`
  ```java
  public record TeacherStudentGradesDTO(
      Integer matriculaId,
      String alumnoNombre,
      String imparticionLabel,
      List<TeacherPeriodoGradesDTO> periodos,
      BigDecimal mediaGlobal)  // weighted mean across periodos' medias by peso
  {}
  ```
- [x] `GradeUpsertRequest.java`
  ```java
  public record GradeUpsertRequest(
      @NotNull Integer matriculaId,
      @NotEmpty List<Entry> entries) {
    public record Entry(
        @NotNull Integer itemEvaluableId,
        @DecimalMin("0.00") @DecimalMax("10.00") BigDecimal valor, // nullable
        @Size(max = 1000) String comentario) {}
  }
  ```

### Task 1.2 — Mapper (`TeacherDashboardMapper`, MapStruct)
- [x] Create `src/main/java/com/tfg/schooledule/infrastructure/mapper/TeacherDashboardMapper.java`, `@Mapper(componentModel="spring")`, with:
  - `TeacherCenterDTO toCenterDto(Centro c, long imparticionesCount);`
  - `TeacherSubjectDTO toSubjectDto(Imparticion i, long alumnosCount);`
  - `TeacherStudentRowDTO toStudentRow(Matricula m);` — build `nombreCompleto = m.alumno.nombre + " " + m.alumno.apellidos` via `@Mapping(target=..., expression="java(...)")`.
  - `TeacherGradeItemDTO toGradeItem(ItemEvaluable item, @Nullable Calificacion existing);` — `valor`, `comentario`, `calificacionId` come from `existing` when non-null, else null.

> The periodo / student aggregate DTOs are **built in the service**, not in the mapper, because they require grouping + arithmetic.

### Task 1.3 — Service `TeacherDashboardService`
- [x] Create `src/main/java/com/tfg/schooledule/infrastructure/service/TeacherDashboardService.java`, `@Service @Transactional(readOnly=true)`:

```java
public List<TeacherCenterDTO> getCentersForTeacher(Integer profesorId);

public List<TeacherSubjectDTO> getSubjectsForTeacherAndCenter(Integer profesorId, Integer centroId);

public List<TeacherStudentRowDTO> getRosterForImparticion(Integer profesorId, Integer imparticionId);
   // MUST assert imparticionRepo.existsByIdAndProfesorId — else throw AccessDeniedException

public TeacherStudentGradesDTO getStudentGrades(Integer profesorId, Integer matriculaId);
   // MUST assert matriculaRepo.findByIdAndImparticionProfesorId present — else AccessDeniedException

@Transactional
public TeacherStudentGradesDTO upsertGrades(
      Integer profesorId, String profesorEmail, GradeUpsertRequest req);
   // 1. Load matricula + check ownership like above.
   // 2. EntityManager.createNativeQuery("SET LOCAL app.current_user = :u").setParameter("u", profesorEmail).executeUpdate();
   // 3. For each entry:
   //    - Load item, assert item.imparticion.id == matricula.imparticion.id (else 400).
   //    - Reject if item.periodoEvaluacion.cerrado == true (throw new IllegalStateException).
   //    - Upsert via calificacionRepo.findByMatriculaIdAndItemEvaluableId(...) → save.
   // 4. Recompute & return the full TeacherStudentGradesDTO (so the UI refreshes with server-authoritative medias).
```

**Averaging rules (server-side, authoritative):**
- `periodo.media = arithmetic mean of non-null valor over items in that periodo, 2-decimal HALF_UP`. Null if all items ungraded.
- `mediaGlobal = Σ(media_i * peso_i) / Σ(peso_i)` over periodos with non-null media; if any periodo has `peso=null`, treat it as equal weight (1). Null if no medias.

**Red tests** (`src/test/java/.../service/TeacherDashboardServiceTest.java`, Mockito):
- `getCentersForTeacher_returnsOnlyLinkedCenters_withImparticionCounts`
- `getSubjectsForTeacherAndCenter_filtersByBothProfesorAndCentro`
- `getRosterForImparticion_throwsAccessDenied_whenTeacherNotOwner`
- `getStudentGrades_groupsItemsByPeriodo_andComputesMedias`
- `upsertGrades_createsNewCalificacion_whenMissing`
- `upsertGrades_updatesExisting_andInvokesSetLocalAppCurrentUser` (verify native query called with the profesor's email)
- `upsertGrades_rejectsWhenPeriodoClosed`
- `upsertGrades_rejectsWhenItemNotInImparticion_400`

---

## Phase 2 — Backend: Controller & REST endpoints [checkpoint: ]

### Task 2.1 — Replace `ProfeController` stub
- [x] Edit `src/main/java/com/tfg/schooledule/infrastructure/controller/ProfeController.java`. Keep `@RequestMapping("/profe")`. Add `@PreAuthorize("hasRole('PROFESOR')")` at class level.

Endpoints:

| Method | Path                                                           | View / returns                                  | Responsibility |
|--------|----------------------------------------------------------------|-------------------------------------------------|----------------|
| GET    | `/profe/dashboard`                                             | `profe/dashboard` (Thymeleaf)                  | Center picker. Model attrs: `centros: List<TeacherCenterDTO>`, `teacherName`. |
| GET    | `/profe/centro/{centroId}/asignaturas`                         | `profe/asignaturas`                             | Subject list for that centro. |
| GET    | `/profe/imparticion/{imparticionId}/alumnos`                   | `profe/alumnos`                                 | Student roster. Model attr `imparticionLabel` from service. |
| GET    | `/profe/api/matricula/{matriculaId}/notas`                     | `@ResponseBody TeacherStudentGradesDTO` (JSON)  | Called by the modal on open. |
| POST   | `/profe/api/matricula/{matriculaId}/notas`                     | `@ResponseBody TeacherStudentGradesDTO` (JSON)  | Body = `GradeUpsertRequest`. Validates `matriculaId` path == body. Returns refreshed payload. |

Every controller method extracts `Principal principal` → `Usuario profesor = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow(...)`, then delegates — ALL ownership checks happen in the service.

**Red tests** (`src/test/java/.../controller/ProfeControllerTest.java`, `@WebMvcTest` + `@WithMockUser(roles="PROFESOR", username="juan@tfg.com")`):
- `dashboard_200_andModelContainsCenters`
- `asignaturas_200_whenCenterOwned`
- `alumnos_403_whenImparticionNotOwned` (service throws `AccessDeniedException`)
- `getNotas_returnsJsonWithExpectedShape` (use `jsonPath("$.periodos[0].media").value(...)`)
- `postNotas_400_whenBodyMatriculaIdMismatchesPath`
- `postNotas_persistsAndReturnsRefreshedPayload`

### Task 2.2 — Global `AccessDeniedException` → 403 JSON for `/profe/api/**`
- [x] Create `src/main/java/com/tfg/schooledule/infrastructure/controller/GlobalApiExceptionHandler.java` with `@RestControllerAdvice(basePackages = "com.tfg.schooledule.infrastructure.controller")` handling:
  - `AccessDeniedException → 403 { "error": "forbidden" }`
  - `MethodArgumentNotValidException → 400 { "error": "validation", "details": [...] }`
  - `IllegalStateException → 409 { "error": "conflict", "message": ... }` (closed periodo)
  - `IllegalArgumentException → 400`

---

## Phase 3 — Frontend: Center picker & Subject list (pages 1 & 2) [checkpoint: ]

> **Design brief** (apply to every template in phases 3 & 4):
> - Load Fraunces (400, 600, 900) + Geist (400, 500, 700) from Google Fonts in a single `<head>` link.
> - Define a root CSS scope `--paper:#F7F2EA; --ink:#1B1F2A; --ink-soft:#3D4356; --accent:#2E3A5C; --hot:#C24E3D; --rule:#1B1F2A14;` in a shared `src/main/resources/static/css/profe.css`.
> - Numerals MUST use `font-feature-settings: "lnum","tnum";` in tables and `"onum","pnum";` in headings for editorial feel.
> - No purple gradients. No stock cards with drop-shadows. Use 1px hairline rules (`var(--rule)`) and asymmetric column grids (12-col, content often spans 8-10 offset by 1).
> - Animations: single staggered fade-in on page load (`@keyframes rise` with `translateY(14px) → 0`, delays 40ms increments per row). No hover scale. Focus states: 2px `var(--hot)` outline offset 3px.
> - Mobile: at `<768px` collapse grid, but keep type scale large (hero h1 minimum `clamp(2.25rem, 6vw, 3.75rem)`).

### Task 3.1 — Shared layout fragment
- [x] Create `src/main/resources/templates/profe/fragments/layout.html` exposing:
  - `<head th:fragment="head(title)">` — includes the `<title>`, Bootstrap reset, Google Fonts, `profe.css`.
  - `<nav th:fragment="topnav(active)">` — editorial masthead (tall, serif wordmark "Schooledule" in Fraunces 900, subtitle "Claustro" in uppercase tracking `letter-spacing: .22em`), links: Dashboard / Cerrar Sesión. The `active` flag toggles a hot-accent underline.
  - `<footer th:fragment="colophon">` — one-line, all-caps: centro seleccionado · curso · fecha. Use `th:text`.

### Task 3.2 — `profe/dashboard.html` (center picker)
- [x] Create `src/main/resources/templates/profe/dashboard.html` (replaces `menuProfesor.html`; **delete** `menuProfesor.html` once controller points elsewhere).
- Layout: hero row (left: serif h1 "Elige un centro", right: small italic body explaining the step). Below, a 2-column list of centros — **no cards**. Each row:
  - Left: 3-digit italic serial (e.g. `—01`).
  - Center: centro `nombre` in Fraunces 600 `clamp(1.5rem,2.5vw,2.25rem)`, ubicacion below in Geist 400 small-caps.
  - Right: `imparticionesCount` in `Fraunces 900` as a giant oldstyle numeral (`font-size: clamp(2rem,4vw,3rem)`). Tooltip: "asignaturas activas".
  - Entire row is a link → `/profe/centro/{id}/asignaturas`. Hover: row background shifts to `#EFE7D8`, hot-accent underline slides under the centro name.
- If no centros: show an editorial empty state (Fraunces italic "Sin asignaciones." + small Geist body "Contacta con la dirección para que te asocien a un centro.").

### Task 3.3 — `profe/asignaturas.html`
- [x] Create `src/main/resources/templates/profe/asignaturas.html`.
- Breadcrumb row: "Centros · {centroNombre}" (Geist 500 uppercase, `letter-spacing:.18em`) — the "Centros" is a back link.
- H1: Fraunces 900 `{centroNombre}`, with a small muted Fraunces italic tagline "Tus asignaturas".
- Listing: a **ledger table** (not a Bootstrap card grid). Columns: código · módulo · grupo · curso · alumnos (right-aligned tabular numerals). Rows linked to `/profe/imparticion/{id}/alumnos`. Use 1px hairline dividers, no zebra stripes. Row hover: the módulo name shifts 6px right with a 160ms ease.

---

## Phase 4 — Frontend: Student roster + Grades modal [checkpoint: ]

### Task 4.1 — `profe/alumnos.html`
- [x] Create `src/main/resources/templates/profe/alumnos.html`. Model: `imparticionLabel` (e.g. "DAW1 · Desarrollo Web en Entorno Cliente") + `List<TeacherStudentRowDTO> alumnos`.
- Hero: breadcrumb "Centros · {centro} · Asignaturas · {label}". H1 = label split on `·` — first part Fraunces 900, second part Fraunces 400 italic.
- Roster: editorial list, NOT a table. Each row is a button (`role="button"` + `tabindex=0`) that opens the modal:
  - Left: serial (01, 02, …) in Fraunces italic.
  - Center: alumno nombre in Fraunces 600 `clamp(1.25rem,1.8vw,1.625rem)`. Email below in Geist mono feature.
  - Right: small chip "Repetidor" in hot-accent outline if `esRepetidor == true`.
  - Keyboard accessible (Enter / Space trigger modal open).
- `data-matricula-id="{{matriculaId}}"` on each row — JS reads it.

### Task 4.2 — Grades modal (HTML + JS)
- [x] Append to `profe/alumnos.html` (or extract as `profe/fragments/gradesModal.html` and include it):

**Markup** (accessible modal, not Bootstrap's JS unless already loaded):
```
<dialog id="gradesModal" aria-labelledby="gradesModalTitle">
  <header>...title + close...</header>
  <section id="gradesBody"> <!-- filled by JS --> </section>
  <footer>
    <span>Media global <output id="mediaGlobal">—</output></span>
    <button id="saveGrades" class="btn-editorial-primary">Guardar</button>
    <button id="closeGrades" class="btn-editorial-ghost">Cerrar</button>
  </footer>
</dialog>
```

**Design inside the modal:**
- Background: `var(--paper)` with a `backdrop-filter: blur(6px)` on `::backdrop`.
- Header: Fraunces 900 student name (fetched), uppercase Geist `imparticionLabel` beneath.
- Body: per periodo, a **section** with:
  - Periodo name (Fraunces 600) + "peso X%" chip + "cerrado" chip (if closed, dim the section to 60% opacity and `pointer-events:none`).
  - A compact ledger: item name · tipo (uppercase small) · fecha · `<input type="number" step="0.01" min="0" max="10">` · `<input type="text">` for comment.
  - Right-aligned periodo media, Fraunces 900, `font-variant-numeric: tabular-nums oldstyle-nums`. Reactively updated on input (`input` event handler recomputes mean on the client; server remains authoritative on save).
- Footer: mediaGlobal right-aligned, buttons left. Primary button has a subtle hot-accent left border 2px.

**JS** (create `src/main/resources/static/js/profe-grades.js`, vanilla, no framework):
```
export function wireRosterClicks() { ... }                 // bind click/keydown on .alumno-row
async function openModal(matriculaId) { ... }              // GET /profe/api/matricula/{id}/notas; render
function renderPeriodo(p) { ... }                          // builds DOM
function recomputeClientMedias() { ... }                   // on every input change
async function saveGrades() { ... }                        // POST; on success re-render with server data
```
Include CSRF token: Thymeleaf emits `<meta name="_csrf" .../>` + `<meta name="_csrf_header" .../>`; JS reads them and sends the header. (Spring Security CSRF is enabled by default on POST.)

**Red tests** (`src/test/java/.../controller/ProfeControllerTest.java` — extend):
- End-to-end smoke via MockMvc: GET modal JSON, POST with one new and one edited grade, GET again → values persisted and averages recomputed.

### Task 4.3 — Accessibility & responsive polish
- [x] Keyboard: modal traps focus; ESC closes; first input auto-focuses. Use `dialog.showModal()`.
- [x] Screen-reader: each input has a visually-hidden `<label>` bound by `for`/`id`, announcing "Nota para {itemNombre} en {periodoNombre}".
- [x] Mobile (<768px): roster serial drops out, chip moves under the name, modal becomes full-screen sheet (slide up animation 220ms).

---

## Phase 5 — Security hardening, integration tests, cleanup [checkpoint: ]

### Task 5.1 — Ownership integration test
- [x] Create `src/test/java/.../controller/ProfeControllerIntegrationTest.java`, `@SpringBootTest @AutoConfigureMockMvc`:
  - Authenticate as profe1. Try to GET `/profe/api/matricula/{id}/notas` for a matricula owned by profe_alumno → expect 403. ✓
  - Authenticate as profe1. POST upsert on a `cerrado=true` periodo → expect 409. ✓
  - Authenticate as alumno1 (ROLE_ALUMNO only). Any `/profe/**` → 403. ✓
  - Note: @MockBean usado para servicio (incompatibilidad H2 NAMED_ENUM con Matricula); verifica SecurityConfig + GlobalApiExceptionHandler en contexto completo.

### Task 5.2 — Audit trail verification
- [x] `AuditoriaNotaRepository` creado + `AuditoriaNotaIntegrationTest` con Testcontainers Postgres. Verifica que el trigger PL/pgSQL escribe en `auditoria_notas` con `usuario_responsable = 'juan@tfg.com'` tras un upsert real. Deps añadidas: `spring-boot-testcontainers`, `testcontainers:junit-jupiter`, `testcontainers:postgresql`.

### Task 5.3 — Remove obsolete files
- [x] Delete `src/main/resources/templates/profe/menuProfesor.html` once unused.
- [x] Remove any `@SuppressWarnings` that became unused.

### Task 5.4 — Coverage gate
- [x] Run `./mvnw clean verify`. JaCoCo: GlobalApiExceptionHandler 100%, ProfeController 97%, TeacherDashboardService 84%. BUILD SUCCESS. 70 tests, 1 skipped (H2 NAMED_ENUM).
- [x] Bug found and fixed: `GradeUpsertRequest.entries` faltaba `@Valid` para validación en cascada de `Entry.valor`.

---

## Quality gates before closing the track
- [x] `./mvnw spotless:apply && ./mvnw verify` green.
- [ ] `pre-commit run --all-files` green.
- [ ] Manual smoke: log in as `juan@tfg.com` / `1234` → dashboard shows 2 centros → pick one → shows subjects → pick one → shows alumno1 → open modal → edit a grade → save → reopen → value persisted → `SELECT * FROM auditoria_notas` shows the change with `usuario_responsable = 'juan@tfg.com'`.
- [ ] Log in as `pedro@tfg.com` (multi-role); pick `PROFESOR`; verify isolation — only sees his own imparticion.
- [ ] Lighthouse (desktop): Accessibility ≥ 95, Performance ≥ 90.

## Design decisions locked (do not reopen)
- **Recuperaciones**: `TipoActividad.RECUPERACION` existe en el enum y el DB ENUM pero no tiene lógica especial. Se trata como un ítem evaluable más dentro de su periodo. El profesor crea el ítem, pone la nota, y la media se recalcula incluyéndola. Lógica de sustitución automática está fuera del scope del TFG.

## Deliverable checklist
- [ ] All tasks above marked `[x] <sha7>`.
- [ ] Each phase has a `[checkpoint: <sha7>]` per `conductor/workflow.md`.
- [ ] `git notes` attached to every task commit + every checkpoint commit.
- [ ] `mem_save` called after each phase with a structured observation (decisions / gotchas).

---

## Appendix A — Data-flow contract (single source of truth for both ends)

```
GET /profe/api/matricula/{id}/notas
  → 200 TeacherStudentGradesDTO

POST /profe/api/matricula/{id}/notas
  body: GradeUpsertRequest { matriculaId, entries:[{itemEvaluableId, valor?, comentario?}] }
  → 200 TeacherStudentGradesDTO (refreshed)
  → 400 validation / mismatched ids / item not in imparticion
  → 403 not owner
  → 409 periodo cerrado
```

## Appendix B — Why `SET LOCAL app.current_user`
`V1__Initial_Schema.sql` lines 195-222 define `trigger_auditoria_notas` which reads `current_setting('app.current_user', true)`. Without setting it per-transaction, audit rows will be logged as `SYSTEM_DB`. Setting it on the EntityManager before any calificaciones UPDATE is MANDATORY for this track — do not bypass by disabling the trigger.

## Appendix C — DO NOT
- Do not change `V1__Initial_Schema.sql` structure (columns, constraints, triggers). Only seed data may be added.
- Do not introduce JS frameworks (React, Vue). Vanilla JS only.
- Do not use inline `style="..."` except for Thymeleaf-driven dynamic values (e.g., an opacity tied to `cerrado`).
- Do not use Bootstrap's `.card` / `.btn-primary` / `.alert-info` look as-is — override or avoid.
- Do not trust path params; the service layer is the authorization boundary.
