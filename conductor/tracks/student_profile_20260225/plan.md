# Implementation Plan - Track student_profile_20260225

This plan follows the TDD-based workflow defined in `conductor/workflow.md`.

## Phase 1: Data Access & Business Logic
- [x] Task: Create DTOs for Alumno Profile and Grade Dashboard [37501d0]
    - [ ] Create `com.tfg.schooledule.domain.DTO.AlumnoProfileDTO`
    - [ ] Create `com.tfg.schooledule.domain.DTO.GradeDTO` and `GradeDashboardDTO`
- [x] Task: Implement Data Retrieval in Repositories [5257bbc]
    - [ ] Add query methods to `UsuarioRepository` to fetch profile-related entities
    - [ ] Add query methods to `CalificacionRepository` to fetch grades for a specific student and period
- [x] Task: Implement Profile Service Logic [e2a18d4]
    - [ ] Write failing tests for `UsuarioService.getAlumnoProfile(Long usuarioId)`
    - [ ] Implement logic to aggregate `Usuario`, `Matricula`, `Grupo`, and `Centro` data into `AlumnoProfileDTO`
    - [ ] Verify tests pass
- [x] Task: Implement Grade Retrieval Service Logic [546230a]
    - [ ] Write failing tests for `UsuarioService.getStudentGrades(Long usuarioId, Long periodoId)`
    - [ ] Implement logic to fetch and map `Calificacion` and `ItemEvaluable` to `GradeDashboardDTO`
    - [ ] Verify tests pass
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md) [ab567e4]

## Phase 2: Web Layer & User Interface
- [x] Task: Implement Profile Controller & View [a43e282]
    - [ ] Update `AlumnoController` with `@GetMapping("/perfil")`
    - [ ] Create `src/main/resources/templates/alumno/perfil.html`
    - [ ] Write integration test for the profile endpoint
- [~] Task: Implement Grade Dashboard Controller & View
    - [ ] Update `AlumnoController` with `@GetMapping("/notas")`
    - [ ] Create `src/main/resources/templates/alumno/dashboard_notas.html`
    - [ ] Implement period selection dropdown and AJAX/Form submission for filtering
    - [ ] Write integration test for the grades endpoint
- [x] Task: Navigation & UI Integration [3f62369]
    - [ ] Update `src/main/resources/templates/alumno/menuAlumno.html` to link to Perfil and Notas
    - [ ] Apply consistent Bootstrap 5 styling and navigation bar
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md) [3f62369]
