package com.mournlied.nutrition_tracker_api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Datos para actualizar la informacion personal del usuario o la usuaria")
public record ActualizarInfoPersonalBaseDTO(
        @Schema(description = "Peso inicial actualizado en gramos (Opcional)", example = "64500")
        Integer pesoInicial,

        @Schema(description = "Nombre(s) y Apellido(s) actualizado (Opcional)", example = "Juan Muñoz")
        String nombre,

        @Schema(description = "Fecha de nacimiento actualizada (Opcional), formato ISO: YYYY-MM-DD", example = "2000-01-01")
        LocalDate nacimiento,

        @Schema(description = "Altura en centimetros actualizada (Opcional)", example = "167")
        Integer altura,

        @Schema(description = "Objetivos actualizados (Opcional)", example = "Peso objetivo: 63 kg")
        String objetivos) {
}
