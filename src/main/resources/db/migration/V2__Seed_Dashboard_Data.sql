-- ==================================================================
-- V2: DATOS SEMILLA PARA EL DASHBOARD DEL PROFESOR
-- Extiende V1 con el segundo centro, imparticiones, matrículas,
-- periodos, ítems evaluables y calificaciones de ejemplo.
-- ==================================================================

-- Centro 2
INSERT INTO centros (id, nombre, ubicacion) VALUES
(2, 'CIFP Norte', 'Bilbao');

-- Sedes: profe1 (id=2) ↔ ambos centros; profe_alumno (id=4) ↔ centro 1
INSERT INTO profesores_sedes (usuario_id, centro_id) VALUES
(2, 1),
(2, 2),
(4, 1);

-- Curso académico
INSERT INTO cursos_academicos (id, nombre, fecha_inicio, fecha_fin, activo) VALUES
(1, '2025/2026', '2025-09-01', '2026-06-30', true);

-- Módulos
INSERT INTO modulos (id, codigo, nombre) VALUES
(1, 'DAW1101', 'Desarrollo Web en Entorno Cliente'),
(2, 'DAW1102', 'Desarrollo Web en Entorno Servidor');

-- Grupos
INSERT INTO grupos (id, nombre, centro_id, curso_academico_id) VALUES
(1, 'DAW1-A', 1, 1),
(2, 'DAW1-B', 1, 1),
(3, 'DAW1-N', 2, 1);

-- Imparticiones
-- profe1: módulo 1 en DAW1-A (centro 1), módulo 2 en DAW1-N (centro 2)
-- profe_alumno: módulo 1 en DAW1-B (centro 1)
INSERT INTO imparticiones (id, modulo_id, grupo_id, profesor_id, centro_id) VALUES
(1, 1, 1, 2, 1),
(2, 2, 3, 2, 2),
(3, 1, 2, 4, 1);

-- Matrículas: alumno1 (id=3) en las 3 imparticiones
INSERT INTO matriculas (id, alumno_id, imparticion_id, centro_id, es_repetidor, estado) VALUES
(1, 3, 1, 1, false, 'ACTIVA'),
(2, 3, 2, 2, false, 'ACTIVA'),
(3, 3, 3, 1, true,  'ACTIVA');

-- Periodos de evaluación
INSERT INTO periodos_evaluacion (id, imparticion_id, nombre, peso, cerrado) VALUES
(1, 1, 'Primer Trimestre',  40.00, false),
(2, 1, 'Segundo Trimestre', 60.00, true),
(3, 2, 'Primer Trimestre',  50.00, false),
(4, 2, 'Segundo Trimestre', 50.00, false),
(5, 3, 'Primer Trimestre',  50.00, false),
(6, 3, 'Segundo Trimestre', 50.00, false);

-- Ítems evaluables
INSERT INTO items_evaluables (id, imparticion_id, periodo_evaluacion_id, nombre, fecha, tipo) VALUES
(1, 1, 1, 'Examen Parcial 1',  '2025-11-15', 'EXAMEN'),
(2, 1, 1, 'Práctica HTML/CSS', '2025-10-20', 'PRACTICA'),
(3, 1, 2, 'Examen Final',      '2026-02-10', 'EXAMEN'),
(4, 3, 5, 'Práctica Node',     '2025-10-25', 'PRACTICA'),
(5, 3, 5, 'Examen Node',       '2025-11-20', 'EXAMEN'),
(6, 3, 6, 'Proyecto Final',    '2026-03-05', 'PRACTICA');

-- Calificaciones pre-pobladas para la matrícula 1
INSERT INTO calificaciones (id, matricula_id, item_evaluable_id, valor, comentario) VALUES
(1, 1, 1, 7.50, 'Buen examen'),
(2, 1, 2, 9.00, 'Excelente práctica');

-- Sincronizar secuencias
SELECT setval('centros_id_seq',             (SELECT MAX(id) FROM centros));
SELECT setval('cursos_academicos_id_seq',   (SELECT MAX(id) FROM cursos_academicos));
SELECT setval('modulos_id_seq',             (SELECT MAX(id) FROM modulos));
SELECT setval('grupos_id_seq',             (SELECT MAX(id) FROM grupos));
SELECT setval('imparticiones_id_seq',       (SELECT MAX(id) FROM imparticiones));
SELECT setval('matriculas_id_seq',          (SELECT MAX(id) FROM matriculas));
SELECT setval('periodos_evaluacion_id_seq', (SELECT MAX(id) FROM periodos_evaluacion));
SELECT setval('items_evaluables_id_seq',    (SELECT MAX(id) FROM items_evaluables));
SELECT setval('calificaciones_id_seq',      (SELECT MAX(id) FROM calificaciones));
