ALTER TABLE grupos
    ADD COLUMN tutor_id INT,
    ADD CONSTRAINT fk_grupos_tutor
        FOREIGN KEY (tutor_id) REFERENCES usuarios(id) ON DELETE SET NULL;

CREATE INDEX idx_grupos_tutor ON grupos(tutor_id);
