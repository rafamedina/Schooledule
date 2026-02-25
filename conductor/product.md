# Initial Concept
The project is "Schooledule", a comprehensive Multi-Site Academic Management Platform (ERP) designed for educational centers. It's built with Java 21, Spring Boot 3.3, and PostgreSQL 16, focusing on data integrity, flexible evaluation, and forensic auditing.

# Product Guide
## Vision
To provide a secure and flexible platform for academic management that ensures the integrity and legal traceability of educational data in complex multi-site environments.

## Target Users
- **System Administrators:** Manage centers, users, and overall system security.
- **Teachers (Professors):** Manage groups, impartition, items for evaluation, and grade entry.
- **Students (Alumnos):** Access grades and enrollment information.
- **Academic Coordinators:** Define evaluation criteria (RAs, Criterios) and monitor academic progress.

## Core Features
- **Multi-Site Isolation:** Physical data isolation via `centro_id` while sharing resources (teachers) across sites.
- **Standard Authentication:** Secure, email-based login system with role-based redirection to specialized dashboards.
- **Hybrid Evaluation Engine:** Flexible grade calculation using JSONB to support various pedagogical criteria (Weighted Criteria, RA averages, etc.).
- **Forensic Audit Module:** Database-level immutable logging (PL/pgSQL triggers) to guarantee the legal validity of academic records.
- **Academic Lifecycle Management:** From student enrollment (`Matricula`) to evaluation and audit.

## Goals
- **Data Integrity & Flexibility:** Ensure data is secure and immutable where necessary while allowing flexible evaluation logic.
- **Multi-Site Scalability:** Support multiple educational centers with distinct but interconnected management.
