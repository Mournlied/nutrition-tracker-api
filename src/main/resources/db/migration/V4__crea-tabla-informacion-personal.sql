CREATE TABLE informacion_personal (
    user_id BIGINT PRIMARY KEY,
    peso_inicial INTEGER,
    nombre VARCHAR(255),
    nacimiento DATE,
    altura INTEGER,
    objetivos VARCHAR(255),
    CONSTRAINT fk_info_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);