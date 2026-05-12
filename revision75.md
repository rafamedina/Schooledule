Feedback de la entrega del 75%

Hola Rafael Medina Ayuso,

Te envío el feedback de tu entrega del 75% de tu TFG TFG - Rafael Medina Ayuso.

Valoración: Excelente

Observaciones del tutor:

Hola Rafa,

He revisado a fondo tu 3ª entrega del TFG y te dejo el feedback por escrito. Tengo bastante que reconocerte y un par de correcciones puntuales para que llegues impecable al Hito 4.
Valoración general

Has hecho un trabajo de altísimo nivel. Has aplicado las 12 recomendaciones que te dejé en la 1ª entrega y, además, has ido sustancialmente más allá implementando piezas que no estaban explícitamente pedidas pero que elevan mucho la calidad del proyecto. Lo resumo así:

    Has mantenido el repositorio desde el 6-abril (correcto, sin cambios de repo)
    Has montado un Git Flow profesional con tres ramas (dev, pre-prod, main), Pull Requests reales con descripciones, uso correcto del flag [skip ci] y promoción automatizada
    Has crecido el proyecto a 200 ficheros: 14 entidades, 30 DTOs separados por uso, 12 controllers, 10 servicios, 11 mappers MapStruct, 5 migraciones Flyway
    Y el cambio más importante: has refactorizado el modelo de notas a granularidad por Criterio de Evaluación, que es como funciona la normativa real de FP. Eso no es añadir features, es alinear el dominio. Bien hecho.

Verificación de las recomendaciones de la 1ª entrega
Lo que has aplicado correctamente

    ✅ Bug principal.getName() corregido (ahora se extrae el username del UserDetails correctamente)
    ✅ login.js eliminado — autenticación centralizada en Spring Security
    ✅ Relación N:M profesores_sedes resuelta con @ManyToMany idiomático y JoinTable correcta
    ✅ Plantillas Admin/Profesor implementadas en Thymeleaf con paneles funcionales (ya no son placeholders "Hola admin")
    ✅ Manejo global de excepciones: GlobalApiExceptionHandler con @RestControllerAdvice cubre MethodArgumentNotValidException, EntityNotFoundException, AccessDeniedException...
    ✅ Contradicción ddl-auto resuelta: validate en producción, esquema íntegramente gestionado por Flyway
    ✅ Migración V1 corregida: el UPDATE previo al INSERT de seed se ha movido a V2 con orden lógico correcto
    ✅ Documentación OpenAPI/Swagger con OpenApiConfig.java, @Operation, @ApiResponse y security scheme Bearer en todos los endpoints
    ✅ Validaciones en DTOs: anotación custom @ValidPassword con PasswordConstraintValidator, @NotBlank, @Email, @Size en todos los Form DTOs
    ✅ DTOs separados por uso (Form/List/Detail) — 30 DTOs estructurados por entidad y caso de uso
    ✅ MapStruct integrado: 11 mappers, eliminando boilerplate
    ✅ Rate limiting con Bucket4j (10 intentos / 15 min) en endpoints sensibles

12 de 12. Sin precedentes en lo que llevo revisado.
Aspectos excepcionales que quiero destacar

Voy a detallar lo que más me ha llamado la atención, porque son cosas que no te había pedido y que hablan muy bien de tu enfoque:

1. Trigger PL/pgSQL forense registrar_cambio_nota()

Sobresaliente. Cada cambio de nota queda registrado en una tabla de auditoría con:

    Usuario que efectúa el cambio (vía variable de sesión app.usuario_actual que fijas al inicio de cada transacción desde Spring)
    Timestamp con zona horaria
    Valor anterior y nuevo
    Tipo de operación (INSERT/UPDATE/DELETE)

Esto cumple con requisitos de trazabilidad forense reales en entornos educativos. En un centro de FP la integridad de la nota es jurídicamente exigible (recursos, reclamaciones), y tu sistema ya lo cubre. 2. Multi-tenant con centroId validado en cada operación

Cada entidad lleva centroId y los servicios validan que la operación se ejecuta contra el centro del usuario autenticado. Cinturón y tirantes: si un Admin del centro A intenta editar un alumno del centro B, el servicio devuelve 403 antes de tocar el repositorio. Estás defendido contra IDOR (Insecure Direct Object Reference) sin haberlo nombrado siquiera. 3. Seguridad en profundidad real

    CSP estricto sin unsafe-inline ni unsafe-eval
    HSTS habilitado
    Double-belt RBAC: @PreAuthorize en métodos de controller y reglas declarativas en SecurityConfig. Si una falla, la otra protege
    CSRF activado para formularios Thymeleaf, desactivado solo en endpoints stateless con JWT
    Cookies con HttpOnly, Secure, SameSite=Lax

4. Pipeline CI/CD productivo

Tu .github/workflows/ci-cd.yml con 4 jobs (lint, build, test, promote-to-preprod) es CI/CD real, no un placeholder. La promoción automatizada dev → pre-prod con auto-PR a main y generación de tags está al nivel de proyectos profesionales. 5. Testcontainers para tests de integración con Postgres real

No tiras de H2 para los tests, usas Postgres en container. Tus tests son representativos de producción de verdad. 6. SonarQube en docker-compose.yml

Lo has levantado. Aunque no esté integrado al pipeline, el hecho de tenerlo arriba implica que has mirado los warnings. 7. @ValidPassword como anotación custom reusable

Implementación limpia: una anotación, un validador, aplicada por toda la app. Patrón perfecto para validaciones complejas. 8. Conventional Commits aplicado consistentemente

feat:, fix:, refactor:, chore:. Eso facilita generación automática de changelogs y revisión de PRs.
Problemas críticos a corregir antes del Hito 4

Son dos y ambos se cierran en menos de una hora:

1. 🚨 Discordancia mayúsculas/minúsculas: Controller/ vs package controller

En infrastructure/Controller/GlobalApiExceptionHandler.java la carpeta está en Controller/ (con mayúscula) pero el package declarado es controller (minúscula). En Windows funciona por casualidad, pero en Linux es case-sensitive y rompe el build.

Tu pipeline corre en Ubuntu y tu producción seguramente también. Que no haya saltado todavía es suerte. Renombra la carpeta a controller/ (todo minúscula) y comprueba si hay otros casos similares (Service/, Repository/, etc.).

git mv infrastructure/Controller infrastructure/controller_tmp
git mv infrastructure/controller_tmp infrastructure/controller

(El doble paso es porque Git en Windows ignora cambios de mayúscula sin esta gimnasia.) 2. 🚨 Hash BCrypt de contraseña "1234" en seed V1

Las migraciones V1 incluyen usuarios seed con BCrypt de la contraseña literal "1234". BCrypt es robusto, pero "1234" se rompe en milisegundos con un diccionario. Si producción se inicializa con estos seed sin un paso de regeneración tras el primer login, queda un Admin con "1234" activo.

Soluciones (cualquiera vale):

    Añadir migración Flyway que ponga must_change_password = true a todos los usuarios seed y forzar cambio en el primer login
    Generar las contraseñas seed aleatorias en runtime durante el bootstrap del primer arranque y volcarlas a un fichero initial-credentials.txt accesible solo al instalador
    Eliminar los usuarios seed y obligar a crear el primer Admin mediante un comando CLI del instalador

Problemas graves (no críticos, pero deben cerrarse para 100%) 3. Propiedad <spring-ai.version> orfanizada en el POM

En tu pom.xml aparece <spring-ai.version> pero ninguna dependencia la consume. Residuo de un experimento. Elimínala — si era para algo futuro, déjala comentada con un TODO explícito. 4. UsuarioDTO orfanizado

Tienes un UsuarioDTO que no se referencia desde ningún controller, servicio ni mapper. Código muerto. Elimínalo. 5. Residuos de RuntimeException en lugar de excepciones de dominio

Aún quedan algunos throw new RuntimeException("mensaje") en servicios donde deberían ser excepciones específicas (EntityNotFoundException, BusinessRuleViolationException...). Tu GlobalApiExceptionHandler las trata como 500 cuando deberían ser 404/422.

Crea una jerarquía mínima:

public class NotFoundException extends RuntimeException { ... }
public class ConflictException extends RuntimeException { ... }
public class BusinessRuleException extends RuntimeException { ... }

Y mapéalas en el handler global a códigos HTTP semánticos. 6. Cobertura de tests desigual en controllers admin nuevos

Los servicios críticos (NotaService, MatriculaService) tienen test, pero los controllers admin que han llegado más tarde no. Apunta a coverage > 70% en módulos críticos para el Hito 4.
Aspectos mejorables menores

    Paginación explícita con Pageable en algunos endpoints admin que devuelven listas completas
    Convención uniforme en nombres de mappers MapStruct (toDto, toEntity, toList, update) — algunos son largos (toListItemFromEntity)
    Un CHANGELOG.md agregado (las notas están en los PRs pero sin agregación)
    Algunas plantillas Thymeleaf duplican fragmentos en lugar de usar th:fragment reutilizables
    README está bien pero un diagrama de arquitectura (C4 nivel 2) elevaría la memoria de cara al tribunal

Preparación para el Hito 4 (100%) — orden de prioridad

    Fix case-sensitive Controller/ → controller/ (2 minutos, crítico para Linux)
    Saneamiento del seed con contraseña "1234" (30 minutos)
    Eliminar código muerto: UsuarioDTO, spring-ai.version del POM, residuos de RuntimeException (1 hora)
    Cubrir con tests los controllers admin nuevos hasta coverage > 70%
    Diagrama de arquitectura (C4 nivel 2) en la memoria final
    Captura de SonarQube con quality gate y deuda técnica para anexo

Recomendaciones para la defensa ante el tribunal

    Prepara demo guiada con tres escenarios: "día de un Admin", "día de un Profesor", "día de un Alumno"
    Ten captura de SonarQube con quality gate aprobado y deuda técnica baja
    Demuestra la auditoría forense en vivo: crea una nota, modifícala desde otro usuario, abre la tabla de auditoría y enseña el registro completo. Va a impresionar
    Ten captura del pipeline CI/CD ejecutándose, mejor en vivo desde GitHub Actions
    Menciona explícitamente las decisiones de seguridad: CSP, RBAC doble, rate limiting, multi-tenant con centroId. Son puntos de nota
    Justifica el refactor a granularidad por Criterio de Evaluación: que el tribunal vea que has alineado el modelo de datos con la normativa real de FP, no con un esquema "de libro"

Conclusión

Rafa, te he revisado en detalle y te lo digo claro: tu 3ª entrega es de un nivel muy alto. Has aplicado todas las recomendaciones de la 1ª entrega y has ido sustancialmente más allá implementando pipeline CI/CD, Git Flow profesional, auditoría forense PL/pgSQL, multi-tenant, rate limiting, CSP estricto y un refactor consciente del modelo de notas.

Los problemas críticos que te quedan son menores y se cierran en menos de una hora (un rename de carpeta y un fix de seed). El resto son refinamientos.

Si el Hito 4 cierra esos dos críticos triviales, añade el diagrama de arquitectura y refina cobertura de tests, esta entrega aspira a la franja alta de calificación del tribunal. Tienes mucho margen para presentarte con tranquilidad.

Si quieres que veamos juntos cualquiera de las correcciones, me dices y montamos tutoría.
