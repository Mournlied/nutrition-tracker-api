package com.mournlied.nutrition_tracker_api.domain.comida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "Datos para registrar una nueva entrada de comida")
public record RegistroComidaDTO(
        @Schema(description = "Nombre de la comida", example = "Filete de pollo a la parrilla")
        @NotBlank
        String nombreComida,

        @Schema(description = "Cantidad en gramos", example = "150")
        @NotNull
        Integer cantidadEnGramos,

        @Schema(description = "Descripción adicional o notas", example = "Sasonado con sal y pimienta")
        String descripcion,

        @Schema(description = "Tipo de comida", example = "Desayuno", allowableValues = {"Desayuno", "Almuerzo", "Cena", "Snack"})
        @NotNull
        String tipoComida,

        @Schema(description = "Información nutricional", example = """
                {
                    "calorias": 165,
                    "proteinas": 31,
                    "carbohidratos":
                                    {
                                        "totales": 0,
                                        "azucares": 0
                                    },
                    "grasa":
                             {
                                "saturada": 3.6,
                                "trans": 0
                             },
                    "fibra": 0,
                    "sodio": 74
                }
                """)
        @NotEmpty
        Map<String, Object> informacionNutricional,

        @Schema(description = "Si desea marcar la comida como favorita", example = "true")
        @NotNull
        Boolean esFavorita) {
}
