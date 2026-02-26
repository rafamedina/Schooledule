# Specification - Track implement_user_view_20260226

This track implements the primary user-facing views for the "Schooledule" platform, specifically focusing on the Role Selection process and the Alumno (Student) Dashboard.

## Overview
The goal is to provide a seamless transition from login to role selection and finally to the student-specific workspace. The views will use a modern sidebar-based navigation layout with Bootstrap 5.

## Functional Requirements
1. **Role Selection View**:
    - A context-selection screen triggered after login if the user has multiple roles (e.g., Alumno and Profesor).
    - Clear buttons/cards to select the active role for the session.
2. **Alumno Dashboard**:
    - A central landing page for students with a summary of their current academic status.
    - **Grade Summary**: A visual overview of recent grades or overall progress.
    - **Weekly Schedule**: A grid or list showing the current week's classes and locations.
3. **Alumno Profile View**:
    - Dedicated page to view and (optionally) update non-academic personal data (email, phone, etc.).
    - Display academic information like enrollment year and assigned group.
4. **Navigation Framework**:
    - Implementation of a fixed sidebar navigation menu using Bootstrap 5.
    - Responsive design ensuring usability on mobile and desktop.

## Non-Functional Requirements
- **Security**: Access to these views must be protected by Spring Security (ROLE_ALUMNO).
- **Usability**: Adhere to the "Product Guidelines" for clean, accessible UI.
- **Performance**: Use efficient queries to load dashboard data (grades/schedule).

## Acceptance Criteria
- [ ] Role Selection screen appears correctly for multi-role users.
- [ ] Selecting "Alumno" redirects to the student dashboard.
- [ ] Sidebar navigation allows switching between Dashboard and Profile without page reload (or via standard Thymeleaf fragments).
- [ ] Dashboard displays dynamic data (placeholder or mock data initially) for grades and schedule.
- [ ] Layout is responsive (Bootstrap 5 grid).

## Out of Scope
- AI Bot assistant integration (deferred to a future track).
- Admin and Professor specialized dashboards.
- Detailed "Enrollment" (Matrícula) management forms.
