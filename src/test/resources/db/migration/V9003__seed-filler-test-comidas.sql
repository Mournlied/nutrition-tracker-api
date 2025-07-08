INSERT INTO comidas(user_id, comida_creacion, cantidad_gramos, comida_descripcion, tipo_comida, info_nutricional, nombre_comida)
SELECT
    u.user_id,
    CURRENT_DATE - (s - 1),
    s * 100,
    'comida numero ' || s,
    CASE (s % 4)
            WHEN 0 THEN 'Desayuno'
            WHEN 1 THEN 'Almuerzo'
            WHEN 2 THEN 'Cena'
            WHEN 3 THEN 'Snack'
        END,
    jsonb_build_object(
            'calorias totales', 150 + s * 10,
            'proteinas', 10 + s,
            'carbohidratos', jsonb_build_object(
                'totales', 30 + s * 2,
                'azucares', 5 + (s % 3) * 2
            ),
            'grasa', jsonb_build_object(
                'saturada', 3 + (s % 4),
                'trans', (s % 2)
            )
        ),
    'comida numero ' || s
FROM
    generate_series(1, 9) s,
    users u
WHERE u.correo = 'admin1@mournlied.com';