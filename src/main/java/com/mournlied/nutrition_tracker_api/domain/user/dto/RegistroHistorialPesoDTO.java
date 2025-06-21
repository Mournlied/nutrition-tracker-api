package com.mournlied.nutrition_tracker_api.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record RegistroHistorialPesoDTO(
        @NotNull
        Integer pesoActual) {
}
