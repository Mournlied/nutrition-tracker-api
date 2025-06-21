package com.mournlied.nutrition_tracker_api.domain.user.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@NotNull
public record ActualizarInfoPersonalBaseDTO(
        Integer pesoInicial,
        String nombre,
        LocalDate nacimiento,
        Integer altura,
        String objetivos) {
}
