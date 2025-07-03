INSERT INTO users (correo, rol_id, estado, user_creacion)
SELECT
    'user1@mournlied.com',
    r.rol_id,
    1,
    CURRENT_DATE
FROM roles r
WHERE r.nombre_rol = 'USER';