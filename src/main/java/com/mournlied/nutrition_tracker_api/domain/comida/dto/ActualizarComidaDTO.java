package com.mournlied.nutrition_tracker_api.domain.comida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "Datos para actualizar una comida ya existente")
public record ActualizarComidaDTO(
        @Schema(description = "Nombre de la comida que se desea actualizar", example = "Nombre original")
        @NotBlank
        String nombreComidaOriginal,

        @Schema(description = "Nombre actualizado para la comida (Opcional)", example = "Nombre nuevo")
        String nombreComidaNuevo,

        @Schema(description = "Cantidad actualizada en gramos (Opcional)", example = "200")
        Integer cantidadEnGramos,

        @Schema(description = "Descripcion actualizada (Opcional)", example = "Nueva descripcion")
        String descripcion,

        @Schema(description = "Tipo de comida actualizada (Opcional)", example = "Cena")
        String tipoComida,

        @Schema(description = "Información nutricional actualizada (Opcional)", example = """
                {
                    "calorias": 180,
                    "proteinas": 40,
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
                }
                """)
        Map<String, Object> informacionNutricional,

        @Schema(description = "Cambiar el estado de favorita (Opcional)", example = "false")
        Boolean esFavorita) {
}
