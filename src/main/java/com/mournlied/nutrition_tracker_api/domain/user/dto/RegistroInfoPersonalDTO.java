package com.mournlied.nutrition_tracker_api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Datos para registro inicial de información personal para un usuario o usuaria")
public record RegistroInfoPersonalDTO(
        @Schema(description = "Peso inicial en gramos", example = "65500")
        @NotNull
        Integer pesoInicial,

        @Schema(description = "Nombre(s) y Apellido(s) (Opcional)", example = "Juan Jose Muñoz")
        String nombre,

        @Schema(description = "Fecha de nacimiento, formato ISO: YYYY-MM-DD", example = "2000-01-01")
        @NotNull
        LocalDate nacimiento,

        @Schema(description = "Altura en centimetros", example = "167")
        @NotNull
        Integer altura,

        @Schema(description = "Objetivos actuales de la usuaria o usuario (Opcional)", example = "Peso objetivo: 63 kg")
        String objetivos) {
}
