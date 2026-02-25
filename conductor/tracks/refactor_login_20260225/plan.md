# Implementation Plan - Track refactor_login_20260225

This plan refactors the authentication flow to use standard Spring Security Filter Chain with role-based redirection.

## Phase 1: Security Configuration & Handlers [checkpoint: fe0ce84]
- [x] Task: Refactor CustomLoginSuccessHandler 74e45ee
    - [ ] Write unit tests for `CustomLoginSuccessHandler` verifying redirects for ADMIN, PROFESOR, and ALUMNO.
    - [ ] Update `com.tfg.schooledule.infrastructure.config.CustomLoginSuccessHandler` to perform logic:
        - If user has ROLE_ADMIN -> redirect to `/admin/dashboard`
        - Else if user has ROLE_PROFESOR -> redirect to `/profe/menuProfesor`
        - Else if user has ROLE_ALUMNO -> redirect to `/alumno/menuAlumno`
    - [ ] Verify tests pass.
- [x] Task: Harden SecurityConfig 0c443db
    - [ ] Write integration tests verifying that `/admin/**` is blocked for non-admins.
    - [ ] Update `SecurityConfig.java`:
        - Ensure `formLogin()` uses `/login` as the page.
        - Ensure `loginProcessingUrl("/login")` is set.
        - Verify `successHandler(successHandler)` is active.
        - Enable CSRF protection (standard security) and update template accordingly.
    - [ ] Verify tests pass.
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Controller & Template Alignment
- [x] Task: Clean up LoginController 63cc002
    - [ ] Remove any manual authentication or session logic that duplicates Spring Security filters.
    - [ ] Keep only the simple `@GetMapping("/login")` to return the view.
    - [ ] Write failing test for login page access.
    - [ ] Verify tests pass.
- [x] Task: Align Login Template 16ed952
    - [ ] Update `src/main/resources/templates/login.html`:
        - Ensure form `action` is `th:action="@{/login}"`.
        - Ensure input names are `username` (for email) and `password`.
        - Add CSRF hidden token: `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>` (if CSRF enabled).
        - Add logic to display error message if `param.error` is present.
    - [ ] Verify visual consistency.
- [ ] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Integration & Testing
- [ ] Task: Full Authentication Integration Test
    - [ ] Write failing integration test simulating a full login flow for each role.
    - [ ] Implement tests using `spring-security-test`.
    - [ ] Verify successful redirection and session creation.
- [ ] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)
