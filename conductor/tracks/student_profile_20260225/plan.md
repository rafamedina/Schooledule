# Implementation Plan - Track student_profile_20260225

This plan follows the TDD-based workflow defined in `conductor/workflow.md`.

## Phase 1: Data Access & Business Logic
- [ ] Task: Create DTOs for Alumno Profile and Grade Dashboard
    - [ ] Create `com.tfg.schooledule.domain.DTO.AlumnoProfileDTO`
    - [ ] Create `com.tfg.schooledule.domain.DTO.GradeDTO` and `GradeDashboardDTO`
- [ ] Task: Implement Data Retrieval in Repositories
    - [ ] Add query methods to `UsuarioRepository` to fetch profile-related entities
    - [ ] Add query methods to `CalificacionRepository` to fetch grades for a specific student and period
- [ ] Task: Implement Profile Service Logic
    - [ ] Write failing tests for `UsuarioService.getAlumnoProfile(Long usuarioId)`
    - [ ] Implement logic to aggregate `Usuario`, `Matricula`, `Grupo`, and `Centro` data into `AlumnoProfileDTO`
    - [ ] Verify tests pass
- [ ] Task: Implement Grade Retrieval Service Logic
    - [ ] Write failing tests for `UsuarioService.getStudentGrades(Long usuarioId, Long periodoId)`
    - [ ] Implement logic to fetch and map `Calificacion` and `ItemEvaluable` to `GradeDashboardDTO`
    - [ ] Verify tests pass
- [ ] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Web Layer & User Interface
- [ ] Task: Implement Profile Controller & View
    - [ ] Update `AlumnoController` with `@GetMapping("/perfil")`
    - [ ] Create `src/main/resources/templates/alumno/perfil.html`
    - [ ] Write integration test for the profile endpoint
- [ ] Task: Implement Grade Dashboard Controller & View
    - [ ] Update `AlumnoController` with `@GetMapping("/notas")`
    - [ ] Create `src/main/resources/templates/alumno/dashboard_notas.html`
    - [ ] Implement period selection dropdown and AJAX/Form submission for filtering
    - [ ] Write integration test for the grades endpoint
- [ ] Task: Navigation & UI Integration
    - [ ] Update `src/main/resources/templates/alumno/menuAlumno.html` to link to Perfil and Notas
    - [ ] Apply consistent Bootstrap 5 styling and navigation bar
- [ ] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)
