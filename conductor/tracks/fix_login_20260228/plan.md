# Implementation Plan - Fix Login Authentication

This plan focuses on diagnosing and fixing the authentication failures across all roles.

## Phase 1: Diagnosis & Testing Infrastructure
- [x] Task: Enable Debug Logging 7c36a57
- [ ] Task: Create Login Reproduction Test
    - [ ] Create `src/test/java/com/tfg/schooledule/infrastructure/config/AuthIntegrationTest.java`.
    - [ ] Write a test case for Admin login (`admin@tfg.com` / `1234`).
    - [ ] Write a test case for Professor login (`juan@tfg.com` / `1234`).
    - [ ] Write a test case for Student login (`ana@tfg.com` / `1234`).
- [ ] Task: Conductor - User Manual Verification 'Diagnosis & Testing Infrastructure' (Protocol in workflow.md)

## Phase 2: Authentication Fix
- [ ] Task: Inspect `Usuario` and `Rol` Entity Mapping
    - [ ] Ensure `roles` are fetched correctly in `CustomUserDetailsService`.
    - [ ] Verify `ROLE_` prefix handling.
- [ ] Task: Fix `CustomUserDetailsService` if needed
    - [ ] Ensure `loadUserByUsername` correctly maps roles to `GrantedAuthority`.
- [ ] Task: Fix `CustomLoginSuccessHandler` if needed
    - [ ] Verify redirection paths for `ROLE_PROFESOR` and `ROLE_ALUMNO`.
- [ ] Task: Conductor - User Manual Verification 'Authentication Fix' (Protocol in workflow.md)

## Phase 3: Final Verification
- [ ] Task: Verify All Dashboards Access
    - [ ] Ensure authenticated users can access their respective restricted areas.
- [ ] Task: Clean up Debug Logging
    - [ ] Revert `application.properties` changes after verification.
- [ ] Task: Conductor - User Manual Verification 'Final Verification' (Protocol in workflow.md)