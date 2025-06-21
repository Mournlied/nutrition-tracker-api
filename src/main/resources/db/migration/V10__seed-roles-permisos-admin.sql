-- Seed roles
INSERT INTO roles (rol_id, nombre_rol) VALUES
    (1, 'USER'),
    (2, 'ADMIN');

-- Seed permisos
INSERT INTO permisos (permiso_id, nombre_permiso) VALUES
    (1, 'LEER_USER_PROPIO'),
    (2, 'LEER_LISTA_USERS'),
    (3, 'MODIFICAR_INFO_PERSONAL'),
    (4, 'MODIFICAR_ESTADO'),
    (5, 'MODIFICAR_ROL'),
    (6, 'ELIMINAR_USER_PROPIO'),
    (7, 'ELIMINAR_USER');

-- Enlaza permisos a rol USER
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.rol_id, p.permiso_id
FROM roles r, permisos p
WHERE r.nombre_rol = 'USER'
  AND p.nombre_permiso IN ('LEER_USER_PROPIO', 'MODIFICAR_INFO_PERSONAL', 'ELIMINAR_USER_PROPIO');

-- Enlaza permisos a rol ADMIN
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.rol_id, p.permiso_id
FROM roles r, permisos p
WHERE r.nombre_rol = 'ADMIN'
  AND p.nombre_permiso IN ('LEER_LISTA_USERS', 'MODIFICAR_ESTADO', 'MODIFICAR_ROL', 'ELIMINAR_USER');

-- Inserta admin user
INSERT INTO users (correo, rol_id, estado, user_creacion)
SELECT
    'admin1@mournlied.com',
    r.rol_id,
    1,
    CURRENT_DATE
FROM roles r
WHERE r.nombre_rol = 'ADMIN';