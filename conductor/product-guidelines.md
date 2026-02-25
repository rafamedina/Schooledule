# Product Guidelines

## Language & Communication
- **Language:** The primary language for user documentation and UI text is Spanish (ES). Code and internal technical documentation should use English (EN).
- **Tone:** Professional, clear, and academic. Avoid jargon unless it's standard educational terminology (e.g., RA, Criterio, Impartición).

## UI/UX Principles
- **Accessibility:** Ensure the platform is accessible to all users, following WCAG standards where possible.
- **Data Clarity:** Prioritize clear data visualization for grades and student progress. Use tables and lists effectively.
- **Consistency:** Maintain a consistent look and feel across all views (Admin, Profe, Alumno) using Bootstrap 5 components.
- **Feedback:** Provide immediate feedback on actions (e.g., "Nota guardada con éxito", "Error al procesar la matrícula").

## Data & Security
- **Integrity First:** Every change to evaluation data must be auditable.
- **Privacy:** Adhere to GDPR/LOPD requirements when handling student and teacher personal information (PII).
- **Isolation:** Never leak data between centers; use `centro_id` filtering globally.

## Development Standards
- **Component-Based:** Use Thymeleaf fragments for reusable UI components.
- **Service-Oriented:** Keep controllers thin; put logic in the service layer (`Service` directory).
- **Database Logic:** Use PL/pgSQL triggers for auditing to ensure the audit log is immutable from the application layer.
