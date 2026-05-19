# Track: Actualización Memoria TFG — Nuevos Requisitos (R14–R21)

**Fecha:** 2026-05-19
**Estado:** ⬜ PENDIENTE
**Archivo objetivo:** `memoria/Memoria_TFG_RafaelMedina.tex`
**Última edición de la memoria:** commit `846a950` (2026-04-21)
**Commits a documentar:** `846a950..HEAD` (35 commits, 8 bloques funcionales)

---

## Contexto y reglas

- La memoria usa el formato **RFTP**: Requisito → Función → Tarea → Prueba.
- Los requisitos existentes van de R01 a R13. Los nuevos empiezan en **R14**.
- Cada `\section` = un Requisito (Rxx).
- Cada `\subsection` = una Función (RxxFyy).
- Cada `\subsubsection` = una Tarea (RxxFyyTzz).
- Cada ítem dentro de `\begin{itemize}` de una tarea = una Prueba (RxxFyyTzzPww).
- **Nunca inventar.** Todo lo que está aquí se puede verificar en el código fuente.
- El bloque de inserción es **antes de** `\chapter{Seguridad}` (línea 993 del .tex actual).
- El separador entre secciones es `%-------------------------------------------------------------`.

---

## FASE 1 — R14: Exportación de auditoría a Excel

**Qué se implementó:**
Un endpoint en `AdminAuditoriaController` que genera un fichero `.xlsx` con Apache POI.
- El ADMIN global descarga **todos** los registros de `auditoria_notas`.
- El ADMIN_CENTRO descarga **solo** los registros de los centros que tiene asignados.
- La respuesta HTTP lleva `Content-Disposition: attachment; filename="auditoria.xlsx"` y `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.

**Columnas del Excel generado:** fecha del cambio, alumno (nombre + apellidos), módulo, valor anterior, valor nuevo, email del profesor responsable.

**Verificación en código:**
- `AdminAuditoriaController.java` — método `exportar()`
- `AdminAuditoriaService.java` — método que construye el `Workbook` con Apache POI
- Dependencia en `pom.xml`: `org.apache.poi:poi-ooxml`

**LaTeX a insertar** (después del bloque `R13`, antes de `\chapter{Seguridad}`):

```latex
%-------------------------------------------------------------
\section{R14 -- El administrador debe poder exportar el registro de auditoría a un fichero Excel}

El panel de auditoría incorpora la capacidad de descarga del historial completo de cambios en calificaciones en formato \texttt{.xlsx}, facilitando el análisis externo y el archivado de expedientes fuera de la plataforma.

\subsection{R14F01 -- El sistema debe generar un fichero Excel con los registros de auditoría}

El fichero se produce en memoria mediante Apache POI y se devuelve como flujo de descarga directa, sin almacenamiento temporal en disco.

\subsubsection{R14F01T01 -- Implementar \texttt{GET /admin/auditoria/exportar} con generación de \texttt{.xlsx}}
Desarrollar en \texttt{AdminAuditoriaController} el endpoint que invoca al servicio de exportación, establece las cabeceras \texttt{Content-Disposition: attachment} y \texttt{Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}, y escribe el \texttt{Workbook} de Apache POI directamente en el \texttt{HttpServletResponse}.
\begin{itemize}
  \item \textbf{R14F01T01P01 --} Descargar el fichero desde el panel del administrador global y verificar que el \texttt{.xlsx} contiene una fila por cada registro de \texttt{auditoria\_notas}, con las columnas: fecha del cambio, nombre del alumno, módulo, valor anterior, valor nuevo y email del profesor responsable.
\end{itemize}

\subsection{R14F02 -- El ADMIN\_CENTRO solo debe exportar los registros de sus centros asignados}

La exportación aplica el mismo filtro de aislamiento por centro que el listado de auditoría: un administrador de centro no puede acceder a los registros de otras sedes.

\subsubsection{R14F02T01 -- Filtrar los registros exportados por los centros del ADMIN\_CENTRO}
El servicio de exportación comprueba el rol del usuario autenticado; si es \texttt{ROLE\_ADMIN\_CENTRO}, obtiene los identificadores de sus centros asignados y filtra la consulta a \texttt{auditoria\_notas} por \texttt{imparticion.centro\_id IN (centros del admin)}.
\begin{itemize}
  \item \textbf{R14F02T01P01 --} Descargar la exportación con sesión de ADMIN\_CENTRO y verificar que el fichero no contiene ningún registro de centros no asignados al administrador autenticado.
\end{itemize}
```

---

## FASE 2 — R15: Filtros y curso académico activo en el panel de administración

**Qué se implementó:**
- `AdminCursoActivoService`: servicio que determina qué `CursoAcademico` es el activo. Un administrador puede marcar un curso como activo mediante `POST /admin/cursos/{id}/activar`. Solo puede haber un curso activo a la vez.
- Todas las pestañas del panel de administración (centros, grupos, módulos, alumnos, imparticiones, auditoría) aceptan parámetros de filtro opcionales vía `@RequestParam`.
- Por defecto, las listas muestran solo los datos del curso académico activo.
- Si no hay ningún curso marcado como activo, se muestran todos los registros sin filtrar.

**Verificación en código:**
- `AdminCursoActivoService.java` — método `getCursoActivo()`
- `AdminCursoAcademicoController.java` — endpoint `POST /{id}/activar`
- Todos los controladores admin tienen `@RequestParam(required = false) Integer cursoAcademicoId`
- Repositorios con queries JPQL que aplican `WHERE ca.id = :cursoId` cuando el parámetro está presente

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R15 -- El panel de administración debe permitir filtrar por curso académico y otros parámetros}

Las listas del panel de administración son demasiado densas en entornos con varios años lectivos. El sistema introduce un mecanismo de filtrado dinámico que reduce el conjunto visible a los datos relevantes del periodo en curso.

\subsection{R15F01 -- El sistema debe mantener un único curso académico activo como referencia de filtrado}

El administrador designa un curso académico como activo. Este curso actúa como valor de filtrado por defecto en todas las pestañas del panel, sin necesidad de seleccionarlo manualmente cada vez.

\subsubsection{R15F01T01 -- Implementar \texttt{POST /admin/cursos/\{id\}/activar} para marcar el curso activo}
Desarrollar en \texttt{AdminCursoAcademicoController} el endpoint que invoca \texttt{AdminCursoActivoService.activar(id)}, desmarca cualquier otro curso activo previamente y establece el seleccionado como el nuevo activo.
\begin{itemize}
  \item \textbf{R15F01T01P01 --} Marcar un curso como activo cuando ya existe otro activo y verificar que el curso anterior pierde el estado activo y el nuevo lo adquiere, quedando un único activo en el sistema.
\end{itemize}

\subsection{R15F02 -- Las listas del panel deben aceptar filtros opcionales por parámetro de URL}

Grupos, imparticiones, alumnos y auditoría admiten filtros opcionales (\texttt{cursoAcademicoId}, \texttt{centroId}, \texttt{grupoId}, \texttt{moduloId}). Cuando no se especifica ninguno, se aplica el curso activo como filtro por defecto.

\subsubsection{R15F02T01 -- Añadir \texttt{@RequestParam} de filtrado en los controladores de administración}
Modificar los métodos de listado en \texttt{AdminGrupoController}, \texttt{AdminImparticionController}, \texttt{AdminAlumnoController} y \texttt{AdminAuditoriaController} para aceptar parámetros de filtro opcionales y propagarlos a los repositorios mediante consultas JPQL condicionales.
\begin{itemize}
  \item \textbf{R15F02T01P01 --} Acceder a \texttt{/admin/grupos} sin parámetros y verificar que solo se listan los grupos del curso académico activo. Añadir \texttt{?cursoAcademicoId=\{idOtro\}} y verificar que el listado cambia para mostrar los grupos del curso indicado.
\end{itemize}
```

---

## FASE 3 — R16: Rol ADMIN\_CENTRO — administrador de centro (Tier-2)

**Qué se implementó:**
Nuevo rol `ROLE_ADMIN_CENTRO` en el sistema RBAC. Los usuarios con este rol acceden a `/centro-admin/**` y pueden realizar CRUD sobre los recursos de **sus centros asignados únicamente**.

- `CentroAdminContextService`: valida en cada operación que el recurso solicitado pertenece a uno de los centros del administrador. Lanza `AccessDeniedException` si no es así.
- Controladores implementados: `CentroAdminGrupoController`, `CentroAdminAlumnoController`, `CentroAdminImparticionController`, `CentroAdminUsuarioController`.
- El aislamiento se aplica a grupos, imparticiones, matrículas y usuarios.
- No puede crear usuarios con rol ADMIN ni ADMIN_CENTRO (usuarios protegidos).

**Verificación en código:**
- `SecurityConfig.java`: `.requestMatchers("/centro-admin/**").hasRole("ADMIN_CENTRO")`
- `CentroAdminContextService.java`: métodos `validateGrupoBelongsToCentroAdmin`, `validateImparticionBelongsToCentroAdmin`, `validateMatriculaBelongsToCentroAdmin`, `validateUsuarioGestionablePorCentroAdmin`
- Cuatro controladores en `infrastructure/controller/CentroAdmin*.java`

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R16 -- El sistema debe disponer de un rol de administrador de centro con alcance limitado a sus sedes asignadas}

Las instituciones educativas con múltiples sedes necesitan delegar parte de la administración a responsables locales sin otorgarles acceso global al sistema. El rol \texttt{ADMIN\_CENTRO} cubre esta necesidad.

\subsection{R16F01 -- El ADMIN\_CENTRO debe poder gestionar grupos dentro de sus centros asignados}

\subsubsection{R16F01T01 -- Implementar el CRUD de grupos en \texttt{/centro-admin/grupos}}
Crear \texttt{CentroAdminGrupoController} con los endpoints de listado, creación, edición y eliminación de grupos. Cada operación invoca a \texttt{CentroAdminContextService} para validar que el grupo pertenece a un centro asignado al administrador autenticado.
\begin{itemize}
  \item \textbf{R16F01T01P01 --} Intentar acceder a \texttt{/centro-admin/grupos/\{grupoId\}/editar} con un \texttt{grupoId} que pertenece a un centro no asignado al administrador y verificar que el sistema devuelve HTTP 403.
\end{itemize}

\subsection{R16F02 -- El ADMIN\_CENTRO debe poder gestionar imparticiones dentro de sus centros asignados}

\subsubsection{R16F02T01 -- Implementar el CRUD de imparticiones en \texttt{/centro-admin/imparticiones}}
Crear \texttt{CentroAdminImparticionController} con la misma lógica de validación de propiedad que el gestor de grupos.
\begin{itemize}
  \item \textbf{R16F02T01P01 --} Intentar crear una impartición asignando un centro no gestionado por el ADMIN\_CENTRO y verificar que la operación es rechazada con HTTP 403.
\end{itemize}

\subsection{R16F03 -- El ADMIN\_CENTRO debe poder gestionar las matrículas de alumnos en sus centros}

\subsubsection{R16F03T01 -- Implementar el CRUD de matrículas en \texttt{/centro-admin/alumnos}}
Crear \texttt{CentroAdminAlumnoController} con los endpoints de listado de alumnos, alta y edición de matrículas. La eliminación solo está disponible cuando no existen calificaciones asociadas.
\begin{itemize}
  \item \textbf{R16F03T01P01 --} Acceder a \texttt{/centro-admin/alumnos} con sesión de ADMIN\_CENTRO y verificar que solo se listan alumnos con matrícula en los centros asignados al administrador autenticado.
\end{itemize}

\subsection{R16F04 -- El ADMIN\_CENTRO no debe poder gestionar usuarios con roles de administrador}

\subsubsection{R16F04T01 -- Proteger usuarios con roles ADMIN y ADMIN\_CENTRO de modificaciones por parte del ADMIN\_CENTRO}
En \texttt{CentroAdminContextService.validateUsuarioGestionablePorCentroAdmin}, verificar que el usuario objetivo no tiene ningún rol dentro del conjunto \texttt{\{ROLE\_ADMIN, ROLE\_ADMIN\_CENTRO\}}. Si lo tiene, lanzar \texttt{AccessDeniedException}.
\begin{itemize}
  \item \textbf{R16F04T01P01 --} Intentar editar un usuario con rol ADMIN desde la sesión de un ADMIN\_CENTRO y verificar que el sistema devuelve HTTP 403 sin aplicar ningún cambio.
\end{itemize}
```

---

## FASE 4 — R17: Rol TUTOR de grupo

**Qué se implementó:**
Un usuario con rol `ROLE_PROFESOR` puede ser designado como tutor de un grupo. Como tutor accede a `/tutor/**` con las siguientes capacidades:

- Ver la lista de grupos de los que es tutor: `GET /tutor/grupos`
- Ver los alumnos de un grupo y sus notas (solo lectura): `GET /tutor/grupo/{grupoId}/alumnos`
- Ver el desglose de notas de una impartición: `GET /tutor/imparticion/{imparticionId}/notas`
- Si el tutor también es profesor de la impartición concreta, accede a la edición de notas (redirección a `/profe/imparticion/{id}/alumnos`).

**Verificación en código:**
- `TutorController.java`: controlador en `/tutor/**` con `@PreAuthorize("hasRole('PROFESOR')")`
- `TutorService.java`: métodos `getGruposDeTutor`, `getAlumnosDelGrupo`, `esProfesorDeLaImparticion`
- La entidad `Grupo` tiene el campo `tutor` (FK a `usuarios`)

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R17 -- El sistema debe soportar el rol de tutor de grupo con acceso de solo lectura al expediente}

En los ciclos formativos, el tutor de grupo tiene responsabilidades de seguimiento académico sin participar directamente en la calificación. El sistema modela esta distinción funcional sin crear un nuevo rol en el RBAC.

\subsection{R17F01 -- El tutor debe poder ver los grupos de los que es responsable}

\subsubsection{R17F01T01 -- Implementar \texttt{GET /tutor/grupos}}
Desarrollar en \texttt{TutorController} el método que recupera mediante \texttt{TutorService.getGruposDeTutor} la lista de grupos donde el usuario autenticado figura como tutor (campo \texttt{tutor\_id} en la tabla \texttt{grupos}).
\begin{itemize}
  \item \textbf{R17F01T01P01 --} Acceder a \texttt{/tutor/grupos} con un usuario que es tutor de dos grupos y verificar que el listado muestra exactamente esos dos grupos.
\end{itemize}

\subsection{R17F02 -- El tutor debe poder consultar las notas de los alumnos de su grupo}

\subsubsection{R17F02T01 -- Implementar \texttt{GET /tutor/grupo/\{grupoId\}/alumnos}}
Mostrar el listado de alumnos del grupo con sus notas resumidas por impartición. La vista es de solo lectura: no expone formularios de edición de calificaciones.
\begin{itemize}
  \item \textbf{R17F02T01P01 --} Acceder al listado de alumnos de un grupo como tutor y verificar que no aparece ningún botón ni enlace de edición de calificaciones en la vista.
\end{itemize}

\subsection{R17F03 -- Si el tutor también es profesor de la impartición, debe poder acceder a la edición de notas}

\subsubsection{R17F03T01 -- Detectar la doble condición tutor-profesor y redirigir al cuaderno de notas}
En \texttt{TutorService.esProfesorDeLaImparticion}, verificar si el usuario autenticado tiene una impartición activa en el grupo que está consultando. Si es así, el enlace a esa impartición redirige al cuaderno del profesor en \texttt{/profe/imparticion/\{id\}/alumnos}.
\begin{itemize}
  \item \textbf{R17F03T01P01 --} Acceder al portal tutor con un usuario que es tutor del grupo y además profesor de una de sus imparticiones. Verificar que el enlace de esa impartición redirige al cuaderno del profesor, mientras que las imparticiones en las que no es profesor muestran la vista de solo lectura.
\end{itemize}
```

---

## FASE 5 — R18: Importación masiva de alumnos desde Excel

**Qué se implementó:**
Flujo de tres pasos para importar alumnos en bloque desde una plantilla Excel:

1. **Descarga de plantilla:** `GET /admin/alumnos/plantilla` devuelve un `.xlsx` con las columnas requeridas: `username`, `nombre`, `apellidos`, `email`, `password`, `centro_nombre`, `curso_academico_nombre`, `grupo_nombre`.
2. **Vista previa:** `POST /admin/alumnos/importar/preview` — el administrador sube el fichero; el sistema lo parsea y valida (sin persistir) devolviendo errores por fila.
3. **Confirmación:** `POST /admin/alumnos/importar/confirmar` — solo si la preview no tiene errores; crea los usuarios y los matricula automáticamente en todas las imparticiones del grupo indicado.

**Límite:** 200 alumnos por importación.
**Validaciones por fila:** username sin espacios (max 50), nombre y apellidos obligatorios (max 100), email con formato válido (max 150), password con política `@ValidPassword`, centro/curso/grupo existentes en BD.
**Seguridad:** MIME type validado (`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`); límite de tamaño de fichero por Spring Boot (`spring.servlet.multipart.max-file-size`).

**Verificación en código:**
- `AdminAlumnoController.java`: endpoints `plantilla`, `preview`, `confirmar`
- `UsuarioExcelParserService.java`: parsea el `.xlsx` con Apache POI
- `UsuarioImportValidatorService.java`: valida las filas (25 tests unitarios)
- `UsuarioImportService.java`: persiste usuarios y crea matrículas

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R18 -- El administrador debe poder importar alumnos en bloque desde un fichero Excel}

La alta manual de alumnos uno a uno no es viable al inicio de un curso académico. El sistema ofrece un mecanismo de importación masiva que permite dar de alta hasta doscientos alumnos con sus matrículas en una única operación.

\subsection{R18F01 -- El sistema debe proporcionar una plantilla Excel descargable con el formato requerido}

\subsubsection{R18F01T01 -- Implementar \texttt{GET /admin/alumnos/plantilla}}
Generar con Apache POI un fichero \texttt{.xlsx} con las columnas: \texttt{username}, \texttt{nombre}, \texttt{apellidos}, \texttt{email}, \texttt{password}, \texttt{centro\_nombre}, \texttt{curso\_academico\_nombre}, \texttt{grupo\_nombre}. Devolver el fichero con \texttt{Content-Disposition: attachment}.
\begin{itemize}
  \item \textbf{R18F01T01P01 --} Descargar la plantilla y verificar que el fichero \texttt{.xlsx} contiene exactamente las ocho columnas requeridas en la primera fila y que el resto del fichero está vacío.
\end{itemize}

\subsection{R18F02 -- El sistema debe validar el contenido del fichero antes de persistir ningún dato}

\subsubsection{R18F02T01 -- Implementar la vista previa con \texttt{POST /admin/alumnos/importar/preview}}
Parsear el fichero con \texttt{UsuarioExcelParserService} y ejecutar las validaciones de \texttt{UsuarioImportValidatorService} sin persistir ningún registro. Devolver la lista de errores por fila al administrador. El límite máximo de filas es 200.
\begin{itemize}
  \item \textbf{R18F02T01P01 --} Subir un fichero con una fila que contiene un email con formato inválido y otra con una contraseña que no cumple la política, y verificar que la vista previa muestra exactamente dos errores (uno por cada fila) con el número de fila y el campo afectado.
  \item \textbf{R18F02T01P02 --} Subir un fichero con 201 filas y verificar que el sistema rechaza la importación con un mensaje que indica que se ha superado el límite de 200 alumnos.
\end{itemize}

\subsection{R18F03 -- El sistema debe crear los usuarios y matricularlos automáticamente al confirmar la importación}

\subsubsection{R18F03T01 -- Implementar la confirmación con \texttt{POST /admin/alumnos/importar/confirmar}}
Para cada fila válida: (1) crear el usuario con rol \texttt{ROLE\_ALUMNO} y contraseña cifrada con BCrypt; (2) localizar el grupo por \texttt{centro\_nombre} + \texttt{curso\_academico\_nombre} + \texttt{grupo\_nombre}; (3) crear una \texttt{Matricula} en estado \texttt{ACTIVA} para cada impartición activa del grupo.
\begin{itemize}
  \item \textbf{R18F03T01P01 --} Confirmar la importación de un fichero con tres alumnos válidos y verificar que se han creado tres usuarios en la tabla \texttt{usuarios} y que cada uno tiene matrículas en todas las imparticiones del grupo especificado.
\end{itemize}
```

---

## FASE 6 — R19: Importación de Resultados de Aprendizaje y Criterios de Evaluación desde Excel

**Qué se implementó:**
Flujo de importación para poblar los `ResultadoAprendizaje` y `CriterioEvaluacion` de un módulo desde un fichero Excel.

- **Descarga de plantilla:** `GET /admin/modulos/plantilla` — fichero `.xlsx` con columnas: `codigo_ra`, `descripcion_ra`, `peso_ra`, `codigo_ce`, `descripcion_ce`, `peso_ce`.
- **Importación:** `POST /admin/modulos/{id}/importar` — parsea, valida y persiste. Si ya existen RAs/CEs para ese módulo y curso académico, los sobreescribe.
- **Validaciones:** el fichero debe tener exactamente la cabecera esperada; `peso_ra` y `peso_ce` son numéricos en rango [0,100]; `codigo_ra` y `codigo_ce` son obligatorios y únicos por módulo.
- **MIME type validado:** solo acepta `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.

**Verificación en código:**
- `AdminModuloController.java`: endpoints `plantilla` e `importar`
- `ModuloExcelParserService.java`: parsea la hoja Excel
- `ModuloImportValidatorService.java`: valida cabecera, tipos y rangos
- `AdminModuloService.java`: método `importarDesdeExcel`

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R19 -- El administrador debe poder importar Resultados de Aprendizaje y Criterios de Evaluación desde Excel}

La configuración curricular de un módulo (RAs y CEs) puede ser extensa. El sistema permite importarla desde una plantilla Excel, reduciendo el tiempo de configuración inicial y minimizando los errores de transcripción.

\subsection{R19F01 -- El sistema debe proporcionar una plantilla Excel para RAs y CEs}

\subsubsection{R19F01T01 -- Implementar \texttt{GET /admin/modulos/plantilla}}
Generar con Apache POI un fichero \texttt{.xlsx} con las columnas: \texttt{codigo\_ra}, \texttt{descripcion\_ra}, \texttt{peso\_ra}, \texttt{codigo\_ce}, \texttt{descripcion\_ce}, \texttt{peso\_ce}. Cada fila representa un criterio de evaluación asociado a un resultado de aprendizaje.
\begin{itemize}
  \item \textbf{R19F01T01P01 --} Descargar la plantilla y verificar que el \texttt{.xlsx} contiene exactamente las seis columnas requeridas en la primera fila.
\end{itemize}

\subsection{R19F02 -- El sistema debe validar y persistir los RAs y CEs del fichero}

\subsubsection{R19F02T01 -- Implementar \texttt{POST /admin/modulos/\{id\}/importar} con validación de MIME type y estructura}
Rechazar el fichero si el MIME type no es \texttt{application/vnd.openxmlformats-officedocument.spreadsheetml.sheet} o si la cabecera no coincide exactamente con la plantilla. Si la validación pasa, persistir los \texttt{ResultadoAprendizaje} y \texttt{CriterioEvaluacion} asociados al módulo.
\begin{itemize}
  \item \textbf{R19F02T01P01 --} Subir un fichero con una columna de cabecera incorrecta y verificar que el sistema devuelve un error descriptivo sin persistir ningún registro.
  \item \textbf{R19F02T01P02 --} Subir un fichero válido con tres RAs y sus CEs correspondientes y verificar que se han creado los registros en las tablas \texttt{resultados\_aprendizaje} y \texttt{criterios\_evaluacion} con los datos del fichero.
\end{itemize}
```

---

## FASE 7 — R20: Pesos configurables de RA y CE en el cálculo de calificaciones

**Qué se implementó:**
El cálculo de la nota final de un RA y de la nota global de un módulo usa pesos almacenados en base de datos, configurables por el administrador, en lugar de valores fijos en el código.

- La entidad `ResultadoAprendizaje` tiene el campo `peso` (porcentaje que representa sobre la nota del módulo).
- La entidad `CriterioEvaluacion` tiene el campo `peso` (porcentaje dentro del RA).
- El administrador puede editar estos pesos desde `GET /admin/modulos/{id}/pesos` (formulario) y `POST /admin/modulos/{id}/pesos` (persistencia).
- `TeacherDashboardService` lee los pesos de BD en cada cálculo de nota (no hay constantes hardcoded).

**Verificación en código:**
- `AdminModuloController.java`: métodos `editarPesos` y `actualizarPesos`
- `AdminModuloPesosFormDTO.java`: DTO con listas de RAs y CEs con sus pesos
- `TeacherDashboardService.java`: usa `ra.getPeso()` y `ce.getPeso()` en el cálculo
- Migración Flyway: columna `peso` en `resultados_aprendizaje` y `criterios_evaluacion`

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R20 -- El administrador debe poder configurar los pesos de RAs y CEs usados en el cálculo de calificaciones}

La ponderación de cada resultado de aprendizaje y criterio de evaluación varía entre módulos y centros. El sistema almacena estos pesos en base de datos y permite modificarlos sin necesidad de desplegar código nuevo.

\subsection{R20F01 -- El administrador debe poder editar los pesos de RAs y CEs de un módulo}

\subsubsection{R20F01T01 -- Implementar \texttt{GET} y \texttt{POST /admin/modulos/\{id\}/pesos}}
Desarrollar en \texttt{AdminModuloController} el formulario que carga los \texttt{ResultadoAprendizaje} y \texttt{CriterioEvaluacion} del módulo con sus pesos actuales y permite editarlos. La suma de pesos de los RAs de un módulo no necesita sumar exactamente 100 (el sistema trabaja con pesos relativos).
\begin{itemize}
  \item \textbf{R20F01T01P01 --} Modificar el peso de un RA y confirmar que el valor actualizado persiste en la columna \texttt{peso} de la tabla \texttt{resultados\_aprendizaje}.
\end{itemize}

\subsection{R20F02 -- El cálculo de calificaciones debe usar los pesos almacenados en base de datos}

\subsubsection{R20F02T01 -- Refactorizar \texttt{TeacherDashboardService} para leer pesos desde la entidad}
Sustituir cualquier constante de peso hardcoded en la lógica de cálculo de \texttt{getStudentGrades} por la lectura de los campos \texttt{peso} de las entidades \texttt{ResultadoAprendizaje} y \texttt{CriterioEvaluacion}.
\begin{itemize}
  \item \textbf{R20F02T01P01 --} Modificar el peso de un RA, consultar la nota del alumno desde el portal del profesor y verificar que la calificación mostrada refleja el nuevo peso sin necesidad de reiniciar la aplicación.
\end{itemize}
```

---

## FASE 8 — R21: Tour guiado interactivo para el panel de administración

**Qué se implementó:**
Tour de onboarding de 11 pasos para el panel del administrador usando la librería Driver.js `v1.3.5`, servida desde CDN jsDelivr (`https://cdn.jsdelivr.net/npm/driver.js@1.3.5/`).

- **Auto-trigger:** el tour se lanza automáticamente la primera vez que el administrador accede al dashboard. La clave `schooledule_tour_admin_visto` en `localStorage` impide que se vuelva a lanzar automáticamente en visitas posteriores.
- **Lanzamiento manual:** el botón «Tour guiado» del sidebar navega a `/admin/dashboard?tour=1`, lo que fuerza el inicio del tour independientemente del `localStorage`.
- **11 pasos:** sidebar completo → tarjetas de estadísticas → usuarios → centros → cursos → grupos → módulos → alumnos → imparticiones → auditoría → área de acciones del sidebar.
- **CSP:** `SecurityConfig` permite `https://cdn.jsdelivr.net` en `script-src` y `style-src`.

**Verificación en código:**
- `src/main/resources/static/js/admin-tour.js`: IIFE con el tour de 11 pasos
- `src/main/resources/templates/admin/fragments/layout.html`: etiquetas `<link>` y `<script>` apuntando al CDN
- `src/main/resources/templates/admin/dashboard.html`: carga de `admin-tour.js`
- `SecurityConfig.java` (swaggerFilterChain y securityFilterChain): `script-src 'self' https://cdn.jsdelivr.net`

**LaTeX a insertar:**

```latex
%-------------------------------------------------------------
\section{R21 -- El panel de administración debe incorporar un tour guiado interactivo de onboarding}

La curva de aprendizaje de un nuevo administrador es reducida mediante un tour guiado que explica cada sección del panel en el primer acceso, sin requerir formación adicional.

\subsection{R21F01 -- El tour debe lanzarse automáticamente en el primer acceso y manualmente en los siguientes}

\subsubsection{R21F01T01 -- Implementar el tour con Driver.js e integrar la lógica de auto-trigger en \texttt{admin-tour.js}}
Crear el fichero \texttt{static/js/admin-tour.js} con once pasos que cubren: el sidebar completo, las tarjetas de estadísticas del dashboard, y los enlaces a cada sección del panel (usuarios, centros, cursos, grupos, módulos, alumnos, imparticiones, auditoría). Utilizar \texttt{localStorage} con la clave \texttt{schooledule\_tour\_admin\_visto} para detectar el primer acceso y disparar el tour automáticamente solo en esa ocasión.
\begin{itemize}
  \item \textbf{R21F01T01P01 --} Borrar la clave \texttt{schooledule\_tour\_admin\_visto} del \texttt{localStorage} del navegador, acceder a \texttt{/admin/dashboard} y verificar que el tour se inicia automáticamente en el primer paso.
  \item \textbf{R21F01T01P02 --} Tras completar el tour (clave en \texttt{localStorage} presente), recargar \texttt{/admin/dashboard} y verificar que el tour \textbf{no} se lanza automáticamente.
\end{itemize}

\subsubsection{R21F01T02 -- Añadir el botón «Tour guiado» al sidebar para el lanzamiento manual}
Incluir en el fragmento \texttt{admin/fragments/layout.html} un enlace hacia \texttt{/admin/dashboard?tour=1}. Al detectar el parámetro \texttt{tour=1} en la URL, \texttt{admin-tour.js} inicia el tour inmediatamente sin consultar el \texttt{localStorage}.
\begin{itemize}
  \item \textbf{R21F01T02P01 --} Hacer clic en el botón «Tour guiado» del sidebar desde cualquier página del panel de administración y verificar que el navegador navega a \texttt{/admin/dashboard} y el tour comienza desde el primer paso.
\end{itemize}

\subsection{R21F02 -- La librería Driver.js debe cargarse desde CDN respetando la política de seguridad de contenidos}

\subsubsection{R21F02T01 -- Actualizar la CSP para permitir el CDN jsDelivr}
Modificar \texttt{SecurityConfig} para incluir \texttt{https://cdn.jsdelivr.net} en las directivas \texttt{script-src} y \texttt{style-src} de la \textit{Content-Security-Policy}, tanto en la cadena de filtros principal como en la de Swagger.
\begin{itemize}
  \item \textbf{R21F02T01P01 --} Acceder al dashboard del administrador y verificar en las herramientas de desarrollo del navegador que no aparecen errores de violación de CSP relacionados con \texttt{cdn.jsdelivr.net}.
\end{itemize}
```

---

## FASE 9 — Actualización del capítulo Seguridad

El capítulo de Seguridad existente debe recibir **dos adiciones** al final de la sección «Reglas de Seguridad Transversales», antes de los comentarios `% TODO`.

### 9.1 — Aislamiento del ADMIN\_CENTRO (nuevo párrafo en la sección A01)

**Insertar** al final de la sección `\section{A01 -- Control de Acceso en Profundidad}`, después del bloque «Anti-IDOR en el portal del alumno»:

```latex
\subsection{Validación de propiedad en el portal del ADMIN\_CENTRO}

El componente \texttt{CentroAdminContextService} actúa como guardián centralizado para el rol \texttt{ADMIN\_CENTRO}. Antes de cada operación de lectura o escritura, verifica mediante consulta a base de datos que el recurso solicitado (grupo, impartición, matrícula o usuario) pertenece a uno de los centros asignados al administrador autenticado. Si la verificación falla, lanza \texttt{AccessDeniedException}, que Spring Security traduce a HTTP 403 sin exponer información del recurso.
```

### 9.2 — Validación de ficheros en las importaciones Excel (nueva sección)

**Insertar** al final del capítulo Seguridad, antes de la sección «Reglas de Seguridad Transversales»:

```latex
\section{A04 / A05 -- Validación de Ficheros en las Importaciones Excel}

Los endpoints de importación masiva (\texttt{/admin/alumnos/importar} y \texttt{/admin/modulos/\{id\}/importar}) aplican las siguientes medidas de seguridad sobre los ficheros subidos:

\begin{itemize}
  \item \textbf{Validación de MIME type}: se rechaza cualquier fichero cuyo \texttt{Content-Type} no sea \texttt{application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}, previniendo la subida de ficheros disfrazados con extensión \texttt{.xlsx}.
  \item \textbf{Límite de tamaño}: configurado mediante \texttt{spring.servlet.multipart.max-file-size}, impide la subida de ficheros que puedan causar denegación de servicio por agotamiento de memoria.
  \item \textbf{Límite de filas}: el parser rechaza ficheros con más de 200 filas antes de procesarlos, acotando el tiempo de CPU por petición.
  \item \textbf{Validación de estructura}: la cabecera del fichero se compara contra el esquema esperado antes de parsear ninguna fila de datos.
\end{itemize}
```

---

## Orden de ejecución

| Fase | Contenido | Inserción |
|------|-----------|-----------|
| 1 | R14 — Exportar auditoría Excel | Antes de `\chapter{Seguridad}` |
| 2 | R15 — Filtros y curso activo | Después de R14 |
| 3 | R16 — ADMIN_CENTRO | Después de R15 |
| 4 | R17 — Tutor de grupo | Después de R16 |
| 5 | R18 — Importar alumnos Excel | Después de R17 |
| 6 | R19 — Importar RAs/CEs Excel | Después de R18 |
| 7 | R20 — Pesos configurables | Después de R19 |
| 8 | R21 — Tour guiado Driver.js | Después de R20 |
| 9a | Seguridad A01 ADMIN_CENTRO | Dentro de `\section{A01}`, tras «Anti-IDOR alumno» |
| 9b | Seguridad A04/A05 Excel | Antes de `\section{Reglas de Seguridad Transversales}` |

---

## Punto exacto de inserción en el .tex

```
... (fin de R13, línea ~988 del .tex actual) ...

%=============================================================
% CAPITULO 8 - SEGURIDAD                    ← INSERTAR ANTES DE ESTA LÍNEA
%=============================================================
\chapter{Seguridad}
```

El bloque R14–R21 va entre el cierre de R13 y la línea `\chapter{Seguridad}`.

---

## Verificación final (checklist)

- [ ] R14 insertado correctamente con 2 funciones y 2 pruebas
- [ ] R15 insertado con 2 funciones y 2 pruebas
- [ ] R16 insertado con 4 funciones y 4 pruebas
- [ ] R17 insertado con 3 funciones y 4 pruebas
- [ ] R18 insertado con 3 funciones y 4 pruebas
- [ ] R19 insertado con 2 funciones y 3 pruebas
- [ ] R20 insertado con 2 funciones y 2 pruebas
- [ ] R21 insertado con 2 funciones y 4 pruebas
- [ ] Sección A01 ampliada con ADMIN_CENTRO
- [ ] Nueva sección A04/A05 Excel añadida al capítulo Seguridad
- [ ] Compilar LaTeX y verificar que no hay errores de compilación
- [ ] Verificar que la tabla de contenidos generada muestra R14–R21
