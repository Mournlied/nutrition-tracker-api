package com.mournlied.nutrition_tracker_api.domain.user.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegistroInfoPersonalDTO(
        @NotNull
        Integer pesoInicial,
        String nombre,
        @NotNull
        LocalDate nacimiento,
        @NotNull
        Integer altura,
        String objetivos) {
}
