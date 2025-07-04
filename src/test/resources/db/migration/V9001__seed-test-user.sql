INSERT INTO users (correo, rol_id, estado, user_creacion)
SELECT
    'user1@mournlied.com',
    r.rol_id,
    1,
    CURRENT_DATE
FROM roles r
WHERE r.nombre_rol = 'USER';

INSERT INTO informacion_personal(user_id, peso_inicial,nombre,nacimiento,altura,objetivos)
SELECT
    u.user_id,
    0,
    '',
    CURRENT_DATE,
    0,
    ''
FROM users u
WHERE u.correo = 'user1@mournlied.com';