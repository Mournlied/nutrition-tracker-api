package com.mournlied.nutrition_tracker_api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para registrar una entrada al historial de peso")
public record RegistroHistorialPesoDTO(
        @Schema(description = "Peso actual en gramos", example = "65300")
        @NotNull
        Integer pesoActual) {
}
