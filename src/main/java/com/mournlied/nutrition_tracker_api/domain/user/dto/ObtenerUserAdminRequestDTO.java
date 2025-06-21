package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.User;

import java.time.LocalDate;

public record ObtenerUserAdminRequestDTO(
        Long userId,
        String correo,
        String nombreRol,
        Integer estado,
        LocalDate fechaCreacionUser) {
    public ObtenerUserAdminRequestDTO(User user) {
        this(user.getUserId(),
                user.getCorreo(),
                user.getRol().getNombreRol(),
                user.getEstado(),
                user.getFechaCreacionUser());
    }
}
