package com.mournlied.nutrition_tracker_api.domain.comida.dto;

import com.mournlied.nutrition_tracker_api.domain.comida.Comida;

import java.time.LocalDate;
import java.util.Map;

public record ObtenerComidaDTO(
        String nombreComida,
        LocalDate fechaCreacionComida,
        Integer cantidadEnGramos,
        String descripcion,
        String tipoComida,
        Map<String, Object> informacionNutricional) {
    public ObtenerComidaDTO(Comida comida){
        this(
                comida.getNombreComida(),
                comida.getFechaCreacionComida(),
                comida.getCantidadEnGramos(),
                comida.getDescripcion(),
                comida.getTipoComida(),
                comida.getInformacionNutricional());
    }
}
