ALTER TABLE comidas ADD nombre_comida VARCHAR(100);
UPDATE comidas SET nombre_comida = CONCAT('sin_nombre_', comida_id) WHERE nombre_comida IS NULL;
ALTER TABLE comidas ALTER COLUMN nombre_comida SET NOT NULL;
ALTER TABLE comidas ADD CONSTRAINT nombre_comida_unico UNIQUE (nombre_comida);

ALTER TABLE comidas ADD es_favorita BOOLEAN DEFAULT FALSE;
UPDATE comidas SET es_favorita = FALSE WHERE es_favorita IS NULL;
ALTER TABLE comidas ALTER COLUMN es_favorita SET NOT NULL;