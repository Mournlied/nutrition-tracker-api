CREATE TABLE rol_hierarchy (
    rol_padre_id INT,
    rol_hijo_id INT,
    PRIMARY KEY (rol_padre_id, rol_hijo_id),
    FOREIGN KEY (rol_padre_id) REFERENCES roles(rol_id),
    FOREIGN KEY (rol_hijo_id) REFERENCES roles(rol_id)
);