package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.validaciones.ClaveValida;
import com.mournlied.nutrition_tracker_api.domain.user.validaciones.ValidUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@ValidUser
public record RegistroUserDTO(
        @NotBlank
        @Email
        String correo,
        @ClaveValida
        String clave) {
}
