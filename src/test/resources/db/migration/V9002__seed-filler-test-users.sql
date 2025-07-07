INSERT INTO users (correo, rol_id, estado, user_creacion)
SELECT
    'user' || x || '@mournlied.com',
    r.rol_id,
    0,
    CURRENT_DATE
FROM
    generate_series(2, 9) x,
    roles r
WHERE
    r.nombre_rol = 'USER';

INSERT INTO users (correo, rol_id, estado, user_creacion)
SELECT
    'admin' || y || '@mournlied.com',
    r.rol_id,
    1,
    CURRENT_DATE
FROM
    generate_series(2, 5) y,
    roles r
WHERE
    r.nombre_rol = 'ADMIN';