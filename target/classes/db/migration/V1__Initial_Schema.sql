-- ==================================================================
-- BASE DE DATOS TFG - VERSIÓN FINAL (ROLES N:M + AUDITORÍA PRO)
-- ==================================================================

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- ENUMS (Solo para estados, ya NO para roles)
CREATE TYPE estado_matricula AS ENUM ('ACTIVA', 'BAJA', 'CONVALIDADO');
CREATE TYPE tipo_actividad AS ENUM ('EXAMEN', 'PRACTICA', 'RECUPERACION', 'ACTITUD');

-- ==================================================================
-- 1. GESTIÓN DE IDENTIDAD Y ROLES (MODIFICADO)
-- ==================================================================

-- 1.1 Tabla de Roles (Catálogo)
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       nombre VARCHAR(50) NOT NULL UNIQUE -- 'ADMIN', 'PROFESOR', 'ALUMNO'
);

-- 1.2 Usuarios (Sin columna de rol directa)
CREATE TABLE usuarios (
                          id SERIAL PRIMARY KEY,
                          username VARCHAR(50) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          nombre VARCHAR(100) NOT NULL,
                          apellidos VARCHAR(100) NOT NULL,
                          email VARCHAR(150) UNIQUE,
                          activo BOOLEAN DEFAULT TRUE,
                          fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 1.3 Tabla Intermedia (Muchos a Muchos: Usuarios <-> Roles)
CREATE TABLE usuarios_roles (
                                usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                                rol_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                                PRIMARY KEY (usuario_id, rol_id)
);

-- ==================================================================
-- 2. ESTRUCTURA ORGANIZATIVA
-- ==================================================================

CREATE TABLE centros (
                         id SERIAL PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         ubicacion VARCHAR(200),
                         configuracion JSONB
);

CREATE TABLE cursos_academicos (
                                   id SERIAL PRIMARY KEY,
                                   nombre VARCHAR(20) NOT NULL,
                                   fecha_inicio DATE NOT NULL,
                                   fecha_fin DATE NOT NULL,
                                   activo BOOLEAN DEFAULT FALSE
);

-- Relación Profesor <-> Sede (Contexto de trabajo)
CREATE TABLE profesores_sedes (
                                  usuario_id INT NOT NULL REFERENCES usuarios(id),
                                  centro_id INT NOT NULL REFERENCES centros(id),
                                  PRIMARY KEY (usuario_id, centro_id)
);

-- ==================================================================
-- 3. CURRÍCULO
-- ==================================================================

CREATE TABLE modulos (
                         id SERIAL PRIMARY KEY,
                         codigo VARCHAR(20) NOT NULL,
                         nombre VARCHAR(150) NOT NULL
);

CREATE TABLE resultados_aprendizaje (
                                        id SERIAL PRIMARY KEY,
                                        modulo_id INT NOT NULL REFERENCES modulos(id),
                                        curso_academico_id INT NOT NULL REFERENCES cursos_academicos(id),
                                        codigo VARCHAR(20) NOT NULL,
                                        descripcion TEXT NOT NULL,
                                        peso_sugerido DECIMAL(5,2)
);

CREATE TABLE criterios_evaluacion (
                                      id SERIAL PRIMARY KEY,
                                      resultado_aprendizaje_id INT NOT NULL REFERENCES resultados_aprendizaje(id),
                                      codigo VARCHAR(20) NOT NULL,
                                      descripcion TEXT NOT NULL
);

-- ==================================================================
-- 4. EJECUCIÓN (CON CORTAFUEGOS DE SEGURIDAD)
-- ==================================================================

CREATE TABLE grupos (
                        id SERIAL PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL,
                        centro_id INT NOT NULL REFERENCES centros(id),
                        curso_academico_id INT NOT NULL REFERENCES cursos_academicos(id)
);

CREATE TABLE imparticiones (
                               id SERIAL PRIMARY KEY,
                               modulo_id INT NOT NULL REFERENCES modulos(id),
                               grupo_id INT NOT NULL REFERENCES grupos(id),
                               profesor_id INT NOT NULL REFERENCES usuarios(id),

    -- CORTAFUEGOS: Redundancia para seguridad por filas (RLS)
                               centro_id INT NOT NULL REFERENCES centros(id),

    -- Configuración de evaluación (Pesos, Teoría/Práctica)
                               configuracion_evaluacion JSONB
);

CREATE TABLE matriculas (
                            id SERIAL PRIMARY KEY,
                            alumno_id INT NOT NULL REFERENCES usuarios(id),
                            imparticion_id INT NOT NULL REFERENCES imparticiones(id),

    -- CORTAFUEGOS
                            centro_id INT NOT NULL REFERENCES centros(id),

                            es_repetidor BOOLEAN DEFAULT FALSE,
                            estado estado_matricula DEFAULT 'ACTIVA',
                            UNIQUE(alumno_id, imparticion_id)
);

-- ==================================================================
-- 5. EVALUACIÓN Y CALIFICACIONES
-- ==================================================================

CREATE TABLE periodos_evaluacion (
                                     id SERIAL PRIMARY KEY,
                                     imparticion_id INT NOT NULL REFERENCES imparticiones(id),
                                     nombre VARCHAR(50) NOT NULL,
                                     peso DECIMAL(5,2),
                                     cerrado BOOLEAN DEFAULT FALSE
);

CREATE TABLE items_evaluables (
                                  id SERIAL PRIMARY KEY,
                                  imparticion_id INT NOT NULL REFERENCES imparticiones(id),
                                  periodo_evaluacion_id INT NOT NULL REFERENCES periodos_evaluacion(id),
                                  nombre VARCHAR(100) NOT NULL,
                                  fecha DATE,
                                  tipo tipo_actividad NOT NULL, -- EXAMEN, RECUPERACION...
                                  configuracion_rubrica JSONB
);

CREATE TABLE calificaciones (
                                id SERIAL PRIMARY KEY,
                                matricula_id INT NOT NULL REFERENCES matriculas(id),
                                item_evaluable_id INT NOT NULL REFERENCES items_evaluables(id),
                                valor DECIMAL(5,2),
                                comentario TEXT,
                                fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE(matricula_id, item_evaluable_id)
);

-- ==================================================================
-- 6. AUDITORÍA FORENSE (TRIGGER)
-- ==================================================================

CREATE TABLE auditoria_notas (
                                 id SERIAL PRIMARY KEY,
                                 calificacion_id INT NOT NULL REFERENCES calificaciones(id),
                                 valor_anterior DECIMAL(5,2),
                                 valor_nuevo DECIMAL(5,2),
                                 usuario_responsable VARCHAR(100),
                                 fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 motivo VARCHAR(255)
);

CREATE OR REPLACE FUNCTION registrar_cambio_nota()
RETURNS TRIGGER AS $$
DECLARE
app_user TEXT;
BEGIN
    -- Intentamos leer la variable de sesión inyectada por Spring Boot
BEGIN
        app_user := current_setting('app.current_user', true);
EXCEPTION WHEN OTHERS THEN
        app_user := current_user;
END;

    IF app_user IS NULL OR app_user = '' THEN
        app_user := 'SYSTEM_DB';
END IF;

    IF (TG_OP = 'UPDATE' AND OLD.valor <> NEW.valor) THEN
        INSERT INTO auditoria_notas (calificacion_id, valor_anterior, valor_nuevo, usuario_responsable, motivo)
        VALUES (NEW.id, OLD.valor, NEW.valor, app_user, 'Modificación registrada');
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_auditoria_notas
    AFTER UPDATE ON calificaciones
    FOR EACH ROW
    EXECUTE FUNCTION registrar_cambio_nota();

-- ==================================================================
-- 7. DATOS INICIALES (AJUSTADOS A N:M)
-- ==================================================================

-- 1. Crear Roles
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN'), ('ROLE_PROFESOR'), ('ROLE_ALUMNO');

-- 2. Crear Centros
INSERT INTO centros (nombre, ubicacion) VALUES ('IES Tecnológico Central', 'Madrid');

-- 3. Crear Usuarios
INSERT INTO usuarios (username, password_hash, nombre, apellidos, email) VALUES
                                                                             ('admin', '$2a$12$o2wIsi9ximnjkAY5UqsqM.b9AXW5zT4DqSbfbdw.e/cZuYg8c5Y4O', 'Super', 'Admin', 'admin@tfg.com'),
                                                                             ('profe1', '$2a$12$o2wIsi9ximnjkAY5UqsqM.b9AXW5zT4DqSbfbdw.e/cZuYg8c5Y4O', 'Juan', 'García', 'juan@tfg.com'),
                                                                             ('alumno1', '$2a$12$o2wIsi9ximnjkAY5UqsqM.b9AXW5zT4DqSbfbdw.e/cZuYg8c5Y4O', 'Ana', 'López', 'ana@tfg.com'),
                                                                             ('profe_alumno', '$2a$12$o2wIsi9ximnjkAY5UqsqM.b9AXW5zT4DqSbfbdw.e/cZuYg8c5Y4O', 'Pedro', 'Mix', 'pedro@tfg.com'); -- Usuario híbrido

-- 4. Asignar Roles (AQUÍ ESTÁ LA MAGIA N:M)
-- Admin
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (1, 1);
-- Profe1
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (2, 2);
-- Alumno1
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (3, 3);

-- Pedro es PROFESOR (2) y ALUMNO (3) a la vez
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (4, 2);
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (4, 3);

CREATE TABLE SPRING_SESSION (
                                PRIMARY_ID CHAR(36) NOT NULL,
                                SESSION_ID CHAR(36) NOT NULL,
                                CREATION_TIME BIGINT NOT NULL,
                                LAST_ACCESS_TIME BIGINT NOT NULL,
                                MAX_INACTIVE_INTERVAL INT NOT NULL,
                                EXPIRY_TIME BIGINT NOT NULL,
                                PRINCIPAL_NAME VARCHAR(100),
                                CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
                                           SESSION_PRIMARY_ID CHAR(36) NOT NULL,
                                           ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
                                           ATTRIBUTE_BYTES BYTEA NOT NULL,
                                           CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
                                           CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);