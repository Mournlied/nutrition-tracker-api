CREATE TABLE historial_peso (
    historial_peso_id SERIAL PRIMARY KEY,
    user_id BIGINT,
    peso_registro INTEGER,
    fecha_registro DATE,
    CONSTRAINT fk_historial_info FOREIGN KEY (user_id) REFERENCES informacion_personal(user_id) ON DELETE CASCADE
);