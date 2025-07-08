package com.mournlied.nutrition_tracker_api.domain.comida.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record RegistroComidaDTO(
        @NotBlank
        String nombreComida,
        @NotNull
        Integer cantidadEnGramos,
        String descripcion,
        @NotNull
        String tipoComida,
        @NotEmpty
        Map<String, Object> informacionNutricional,
        @NotNull
        Boolean esFavorita) {
}
