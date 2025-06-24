package com.mournlied.nutrition_tracker_api.domain.user.dto;

import java.time.LocalDate;

public record ActualizarInfoPersonalBaseDTO(
        Integer pesoInicial,
        String nombre,
        LocalDate nacimiento,
        Integer altura,
        String objetivos) {
}
