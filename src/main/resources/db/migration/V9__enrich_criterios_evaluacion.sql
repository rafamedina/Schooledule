-- ============================================================
-- V9: ENRIQUECER CRITERIOS DE EVALUACIÓN PARA IMPORTACIÓN EXCEL
-- Añade: peso, instrumento, unidad_didactica, trimestre
-- Añade: unique constraints en criterios_evaluacion y resultados_aprendizaje
-- ============================================================

ALTER TABLE criterios_evaluacion
    ADD COLUMN peso              NUMERIC(5,2) NOT NULL DEFAULT 0,
    ADD COLUMN instrumento       VARCHAR(50),
    ADD COLUMN unidad_didactica  VARCHAR(20),
    ADD COLUMN trimestre         SMALLINT CONSTRAINT chk_ce_trimestre CHECK (trimestre IN (1, 2));

-- Evitar CEs duplicados dentro del mismo RA
ALTER TABLE criterios_evaluacion
    ADD CONSTRAINT uk_ce_ra_codigo UNIQUE (resultado_aprendizaje_id, codigo);

-- Evitar RAs duplicados dentro del mismo módulo + curso
ALTER TABLE resultados_aprendizaje
    ADD CONSTRAINT uk_ra_modulo_curso_codigo UNIQUE (modulo_id, curso_academico_id, codigo);
