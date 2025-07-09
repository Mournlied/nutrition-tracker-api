package com.mournlied.nutrition_tracker_api.domain.comida.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ActualizarComidaDTO(
        @NotBlank
        String nombreComidaOriginal,
        String nombreComidaNuevo,
        Integer cantidadEnGramos,
        String descripcion,
        String tipoComida,
        Map<String, Object> informacionNutricional,
        Boolean esFavorita) {
}
