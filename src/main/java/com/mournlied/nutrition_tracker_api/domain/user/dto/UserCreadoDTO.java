package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.User;

import java.time.LocalDate;

public record UserCreadoDTO(
        Long userId,
        String correo,
        Integer estado,
        LocalDate fechaCreacionUser
) {
    public UserCreadoDTO(User user) {
        this(   user.getUserId(),
                user.getCorreo(),
                user.getEstado(),
                user.getFechaCreacionUser());
    }
}
