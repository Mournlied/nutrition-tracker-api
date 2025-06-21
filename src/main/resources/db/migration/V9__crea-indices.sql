-- Indices para foreign keys

CREATE INDEX idx_comidas_user_id ON comidas(user_id);
CREATE INDEX idx_historial_peso_user_id ON historial_peso(user_id);
CREATE INDEX idx_info_personal_user_id ON informacion_personal(user_id);
CREATE INDEX idx_rol_hierarchy_rol_padre_id ON rol_hierarchy(rol_padre_id);
CREATE INDEX idx_rol_hierarchy_rol_hijo_id ON rol_hierarchy(rol_hijo_id);
CREATE INDEX idx_rol_permisos_rol_id ON rol_permisos(rol_id);
CREATE INDEX idx_rol_permisos_permiso_id ON rol_permisos(permiso_id);

-- Ayuda a buscar/filtrar

CREATE INDEX idx_users_correo ON users(correo);