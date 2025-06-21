-- Define jerarquia ADMIN hereda de USER
INSERT INTO rol_hierarchy (rol_padre_id, rol_hijo_id)
SELECT r_admin.rol_id, r_user.rol_id
FROM roles r_admin, roles r_user
WHERE r_admin.nombre_rol = 'ADMIN' AND r_user.nombre_rol = 'USER';