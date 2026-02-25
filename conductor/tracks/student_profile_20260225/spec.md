# Track Specification: student_profile_20260225

## Objective
Implement a personal profile view and a comprehensive grade dashboard for students. This will allow students to manage their personal information and track their academic progress across different modules and evaluation periods.

## User Stories
- As a student, I want to see my personal details (name, email, center, enrolled course) so that I can verify my registration information.
- As a student, I want to view my grades for each module and item evaluated so that I can track my academic performance.
- As a student, I want to filter my grades by evaluation period so that I can see my progress over time.

## Requirements
- **Profile View:**
    - Display: Full Name, Email, Username, Assigned Center, Current Academic Year, Enrolled Group/Course.
    - Path: `/alumno/perfil`
- **Grade Dashboard:**
    - List of enrolled modules.
    - For each module: display items evaluated (`ItemEvaluable`), their weights, and the student's grade (`Calificacion`).
    - Display final calculated grade for each period (RA-based or Criteria-based depending on center configuration).
    - Filter by `PeriodoEvaluacion`.
    - Path: `/alumno/notas`
- **Security:**
    - Students must only be able to access their own data.
    - Access control enforced via Spring Security (ROLE_ALUMNO).
    - Data isolation using `usuario_id` linked to the authenticated principal.

## Technical Details
- **Backend:**
    - DTOs: `AlumnoProfileDTO`, `GradeDashboardDTO`.
    - Service: `UsuarioService` to fetch profile data.
    - Repository: `CalificacionRepository`, `MatriculaRepository`.
- **Frontend:**
    - Templates: `alumno/perfil.html`, `alumno/dashboard_notas.html`.
    - Styling: Bootstrap 5, consistent with existing login/menu style.
