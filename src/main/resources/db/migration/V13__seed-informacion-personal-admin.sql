INSERT INTO informacion_personal(user_id, peso_inicial,nombre,nacimiento,altura,objetivos)
SELECT
    u.user_id,
    0,
    '',
    CURRENT_DATE,
    0,
    ''
FROM users u
WHERE u.correo = 'admin1@mournlied.com';