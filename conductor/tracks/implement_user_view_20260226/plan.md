# Implementation Plan - Track implement_user_view_20260226

This track implements the primary user-facing views for the "Schooledule" platform, specifically focusing on the Role Selection process and the Alumno (Student) Dashboard.

## Phase 1: Role Selection Framework [checkpoint: 63a68fb]
- [x] Task: Create Role Selection Template 8a4e812
    - [x] Create `src/main/resources/templates/seleccionar-rol.html`
    - [x] Implement a clean interface using Bootstrap 5 to display available roles (ADMIN, PROFESOR, ALUMNO)
    - [x] Ensure buttons link to the appropriate dashboard with a `role` parameter (or session update)
- [x] Task: Update LoginController for Role Selection 76e356d
    - [x] Write unit tests for `LoginController` to verify redirection to `/seleccionar-rol` if the user has multiple roles
    - [x] Update `LoginController` with a `@GetMapping("/seleccionar-rol")` mapping
    - [x] Implement logic to display only the roles assigned to the current authenticated user
    - [x] Verify tests pass
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md) 63a68fb

## Phase 2: Alumno Dashboard & Sidebar Navigation [checkpoint: ]
- [ ] Task: Create Base Alumno Layout with Sidebar
    - [ ] Create/Update `src/main/resources/templates/alumno/menuAlumno.html` as the base layout
    - [ ] Implement a fixed Sidebar Navigation using Bootstrap 5 (Dashboard, Profile, Grades, Schedule, Logout)
    - [ ] Use Thymeleaf fragments for common components
    - [ ] Write integration test verifying the sidebar is present for ROLE_ALUMNO
- [ ] Task: Implement Alumno Dashboard View
    - [ ] Create `src/main/resources/templates/alumno/dashboard.html` (main landing)
    - [ ] Implement a Summary Dashboard showing a placeholder for academic status
    - [ ] Update `AlumnoController` with `@GetMapping("/dashboard")`
    - [ ] Verify tests pass
- [ ] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Profile & Schedule Views [checkpoint: ]
- [ ] Task: Implement Alumno Profile View
    - [ ] Create `src/main/resources/templates/alumno/perfil.html`
    - [ ] Implement forms/display for student personal data (Name, Email, Phone, Enrollment Details)
    - [ ] Add navigation from the sidebar to the profile view
    - [ ] Write integration tests for profile access and data rendering
- [ ] Task: Implement Weekly Schedule Component
    - [ ] Create a "Weekly Schedule" grid component in the dashboard (using placeholders for now)
    - [ ] Ensure the schedule is responsive and clearly displays class times and locations
    - [ ] Verify visual consistency with the overall design
- [ ] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)
