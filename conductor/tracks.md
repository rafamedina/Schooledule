# Project Tracks

This file tracks all major tracks for the project. Each track has its own detailed plan in its respective folder.

---

## En progreso / Pendientes

- [x] **Track: ux_fixes_20260601** — Error handling (403→login), filtros completos en todas las pestañas del panel admin (Usuarios/Centros/Módulos/Cursos/Auditoría), y email de bienvenida al crear cuenta (manual + Excel).
*Link: [./tracks/ux_fixes_20260601/plan.md](./tracks/ux_fixes_20260601/plan.md)*

- [x] **Track: memoria_actualizacion_20260519** — Añadir R14–R21 a la memoria TFG: ADMIN_CENTRO, tutor, filtros, exportar Excel, importar alumnos, importar RAs/CEs, pesos configurables, tour guiado. + Ampliación capítulo Seguridad.
*Link: [./tracks/memoria_actualizacion_20260519/plan.md](./tracks/memoria_actualizacion_20260519/plan.md)*

- [ ] **Track: onboarding_tour_20260517** — Tour guiado con Driver.js para ADMIN y PROFESOR. Auto-trigger en primer acceso (localStorage), botón "Repetir tour" en sidebar. Librería servida localmente (CSP compliant).
*Link: [./tracks/onboarding_tour_20260517/plan.md](./tracks/onboarding_tour_20260517/plan.md)*

---

## Completados

- [x] **Track: sonarqube_cleanup_20260519** — Eliminar 158 issues SonarQube (67 CRITICAL, 40 MAJOR, 26 MINOR, 25 INFO). Completado 2026-05-19. 158 → 0 issues, deuda 1042 min → 0 min.
*Link: [./tracks/sonarqube_cleanup_20260519/plan.md](./tracks/sonarqube_cleanup_20260519/plan.md)*

- [x] **Track: admin_modulos_refactor_20260517** — Refactor pestaña admin/modulos: unificar flujo via Excel (botón "Importar módulo"), arreglar "Ver resumen", cambiar "Editar" a edición de pesos RA/CE, añadir columna "Cursos con RAs", eliminar botón "Importar RAs" por fila.
*Link: [./tracks/admin_modulos_refactor_20260517/plan.md](./tracks/admin_modulos_refactor_20260517/plan.md)*

- [x] **Track: import_usuarios_masivo_20260517** — Importación masiva de alumnos desde Excel: parser→validator→import service, auto-matrícula en imparticiones del grupo, plantilla descargable, TDD completo, OWASP. Completado 2026-05-17.
*Link: [./tracks/import_usuarios_masivo_20260517/plan.md](./tracks/import_usuarios_masivo_20260517/plan.md)*

- [x] **Track: flyway_consolidacion_v1_definitivo_20260517** — Consolidar V1–V9 en V1 DDL puro + V2 seeds. Eliminar parches, ALTER MID-FLIGHT y datos semilla con esquema antiguo. Instalación limpia en <5 s. Completado 2026-05-17.
*Link: [./tracks/flyway_consolidacion_v1_definitivo_20260517/plan.md](./tracks/flyway_consolidacion_v1_definitivo_20260517/plan.md)*

- [x] **Track: admin_modulo_resumen_20260516** — Modal de resumen por módulo en la pestaña admin: info básica, conteos y RAs+CEs agrupados por curso académico. Endpoint JSON + JS inline.
*Link: [./tracks/admin_modulo_resumen_20260516/plan.md](./tracks/admin_modulo_resumen_20260516/plan.md)*

- [x] **Track: import_modulos_excel_20260516** — Importación de RAs y CEs desde plantilla Excel (.xlsx): parser, validator, servicio de importación, plantilla descargable, TDD completo, OWASP A01/A03/A04/A05. Completado 2026-05-16.
*Link: [./tracks/import_modulos_excel_20260516/plan.md](./tracks/import_modulos_excel_20260516/plan.md)*

- [x] **Track: export_auditoria_excel_20260514** — Exportar logs de auditoría a Excel: ADMIN global (todos los registros) y ADMIN_CENTRO (solo sus centros asignados). Completado 2026-05-14.
*Link: [./tracks/export_auditoria_excel_20260514/plan.md](./tracks/export_auditoria_excel_20260514/plan.md)*

- [x] **Track: admin_filtros_cursoactivo_20260514** — Filtros en todas las pestañas admin (centro, grupo, módulo, año, profesor) + mostrar solo datos del año lectivo activo por defecto. Completado 2026-05-14.

- [x] **Track: centro_admin_20260512** — Admin de Centro (Tier-2): CRUD completo de grupos/imparticiones/alumnos/usuarios, restringido a los centros asignados. Completado 2026-05-12.

- [x] **Track: tutor_grupo_20260512** — Tutor de Grupo: vista de grupos, alumnos y notas (RO); edición solo si también es profesor. Completado 2026-05-12.

- [x] **Track: granular_grading_per_criterio_20260420** — Calificación Granular por CE + Recuperaciones — Completado 2026-05-03.
*Link: [./tracks/granular_grading_per_criterio_20260420/plan.md](./tracks/granular_grading_per_criterio_20260420/plan.md)*

- [x] **Track: implement_user_view_20260226** — Completado 2026-05-02.
*Link: [./tracks/implement_user_view_20260226/plan.md](./tracks/implement_user_view_20260226/plan.md)*

- [x] **Track: Auditoría de Notas — Admin** — Completado 2026-05-02.
*Link: [./tracks/completados/admin_auditoria_notas_plan.md](./tracks/completados/admin_auditoria_notas_plan.md)*

- [x] **Track: Gestión de Imparticiones — Admin** — Completado 2026-05-02.
*Link: [./tracks/completados/admin_gestion_imparticiones_plan.md](./tracks/completados/admin_gestion_imparticiones_plan.md)*

- [x] **Track: Dashboard Estadísticas — Completar** — Completado 2026-05-02.
*Link: [./tracks/completados/admin_dashboard_completar_plan.md](./tracks/completados/admin_dashboard_completar_plan.md)*

- [x] **Track: Gestión de Cursos Académicos — Admin** — Completado 2026-05-02.
*Link: [./tracks/admin_gestion_cursos_academicos_plan.md](./tracks/admin_gestion_cursos_academicos_plan.md)*

- [x] **Track: Gestión de Grupos — Admin** — Completado 2026-05-02.
*Link: [./tracks/admin_gestion_grupos_plan.md](./tracks/admin_gestion_grupos_plan.md)*

- [x] **Track: Gestión de Alumnos — Admin** — Completado 2026-05-02.
*Link: [./tracks/admin_gestion_alumnos_plan.md](./tracks/admin_gestion_alumnos_plan.md)*

- [x] **Track: OWASP Security Hardening** — Completado 2026-04-29.
*Link: [./tracks/completados/owasp_security_hardening_plan.md](./tracks/completados/owasp_security_hardening_plan.md)*

- [x] **Track: Gestión de Usuarios — Admin** — Completado 2026-04-29.
*Link: [./tracks/completados/admin_gestion_usuarios_plan.md](./tracks/completados/admin_gestion_usuarios_plan.md)*

- [x] **Track: Gestión de Centros — Admin** — Completado 2026-04-29.
*Link: [./tracks/completados/admin_gestion_centros_plan.md](./tracks/completados/admin_gestion_centros_plan.md)*

- [x] **Track: Gestión de Módulos — Admin** — Completado 2026-04-29.
*Link: [./tracks/completados/admin_gestion_modulos_plan.md](./tracks/completados/admin_gestion_modulos_plan.md)*

- [x] **Track: implement_teacher_dashboard_20260418** — Completado 2026-04-18.
*Link: [./tracks/implement_teacher_dashboard_20260418/](./tracks/implement_teacher_dashboard_20260418/)*
