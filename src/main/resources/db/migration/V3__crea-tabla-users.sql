CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    correo VARCHAR(255),
    rol_id INTEGER,
    estado INTEGER,
    user_creacion DATE,
    CONSTRAINT fk_user_rol FOREIGN KEY (rol_id) REFERENCES roles(rol_id)
);