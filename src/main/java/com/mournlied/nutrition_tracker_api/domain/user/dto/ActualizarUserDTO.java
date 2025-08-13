package com.mournlied.nutrition_tracker_api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar el rol o estado de la cuenta")
public record ActualizarUserDTO(

        @Schema(description = "Nuevo rolID para la cuenta", example = "2", allowableValues = {"1", "2"})
        Integer rolId,

        @Schema(description = "Modificar el estado de la cuenta (0 = inactiva, 1 = activa)", example = "0", allowableValues = {"0", "1"})
        Integer estado) {
}
