package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.User;


public record ObtenerUserDTO(
        InformacionPersonalDTO infoPersonal,
        String correo,
        Integer estado) {
    public ObtenerUserDTO(User user) {
        this(new InformacionPersonalDTO(
                user.getInfoPersonal()),
                user.getCorreo(),
                user.getEstado());
    }
}
