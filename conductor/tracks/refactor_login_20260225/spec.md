# Track Specification: refactor_login_20260225

## Overview
Refactor the current manual login implementation to leverage Spring Security's standard filter chain mechanism. The goal is to improve security, simplify the authentication logic, and provide seamless role-based redirection to user-specific dashboards.

## Functional Requirements
- **Authentication Identifier:** Users will authenticate using their **Email** address.
- **Custom User Interface:** Continue using the existing `src/main/resources/templates/login.html` file, updated to work with Spring Security's `formLogin()`.
- **Role-Based Redirection:** Implement a `CustomAuthenticationSuccessHandler` to route users after successful login:
    - `ROLE_ADMIN` -> `/admin/dashboard`
    - `ROLE_PROFESOR` -> `/profe/menuProfesor`
    - `ROLE_ALUMNO` -> `/alumno/menuAlumno`
- **Security Configuration:** Update `SecurityConfig.java` to:
    - Define public and protected paths.
    - Configure `formLogin` with the custom page and success handler.
    - Configure `logout` to clear sessions properly.
- **Backend Integration:** Ensure the `CustomUserDetailsService` is correctly wired to fetch users by email and load their associated roles.

## Non-Functional Requirements
- **Maintainability:** Remove manual `LoginController` handling of authentication tokens/logic.
- **Security:** Ensure BCrypt password encoding is consistently applied.
- **State Management:** Leverage Spring Session (JDBC) for consistent session tracking.

## Acceptance Criteria
- [ ] Users can login using a valid email and password via the custom login page.
- [ ] Successful login redirects the user to the dashboard corresponding to their highest priority role.
- [ ] Invalid credentials result in a redirect back to the login page with an error parameter.
- [ ] Protected endpoints are inaccessible to unauthenticated users.
- [ ] Logout terminates the session and redirects to the login page.

## Out of Scope
- Implementation of "Forgot Password" or "Registration" flows.
- Multi-factor authentication (MFA).
- Changes to the physical database schema.
