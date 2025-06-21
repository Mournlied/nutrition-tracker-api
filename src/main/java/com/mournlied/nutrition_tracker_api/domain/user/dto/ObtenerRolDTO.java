package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.Rol;

public record ObtenerRolDTO(
        Integer rolId,
        String nombreRoles) {

    public ObtenerRolDTO(Rol rol) {
        this(
                rol.getRolId(),
                rol.getNombreRol());
    }
}
