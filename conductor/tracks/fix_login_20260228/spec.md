# Specification: Fix Login Authentication

## Overview
Currently, the login process is failing for all users (Admin, Student, Professor). Previously, Admin and Student roles were working, but Professor was not. The goal is to identify the root cause of the authentication failure, enable better error logging/feedback (console or UI), and ensure all roles can log in correctly according to their assigned permissions.

## Functional Requirements
- **FR1: Role-Based Authentication:** Ensure Admin, Student, and Professor can authenticate using their email/credentials.
- **FR2: Error Logging:** Implement or expose detailed error logs in the console to diagnose authentication failures (Spring Security exceptions, database connection issues, etc.).
- **FR3: Role-Based Redirection:** Verify that after a successful login, users are redirected to their respective dashboards (`/admin/dashboard`, `/alumno/dashboard`, `/profe/menuProfesor`).
- **FR4: Error Feedback:** Provide meaningful feedback on the login page if the authentication fails (e.g., "Invalid credentials", "Account locked", etc.).

## Non-Functional Requirements
- **NFR1: Security:** Maintain Spring Security 6 best practices for password hashing and session management.
- **NFR2: Traceability:** Ensure authentication attempts are logged for auditing purposes (if required by the Forensic Audit Module).

## Acceptance Criteria
- [ ] Admin user can log in and reach the admin dashboard.
- [ ] Student user can log in and reach the student dashboard.
- [ ] Professor user can log in and reach the professor dashboard.
- [ ] Authentication failures are clearly logged in the application console with the specific exception (e.g., `BadCredentialsException`, `UsernameNotFoundException`).
- [ ] The login page displays a generic "Invalid credentials" message to the user for security, but the console shows the detailed reason.

## Out of Scope
- Password reset functionality.
- Multi-factor authentication (MFA).