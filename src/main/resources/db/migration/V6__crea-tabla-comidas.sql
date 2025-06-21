CREATE TABLE comidas (
    comida_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    comida_creacion DATE,
    cantidad_gramos INTEGER,
    comida_descripcion VARCHAR(255),
    tipo_comida VARCHAR(255),
    info_nutricional JSONB,
    CONSTRAINT fk_comida_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);