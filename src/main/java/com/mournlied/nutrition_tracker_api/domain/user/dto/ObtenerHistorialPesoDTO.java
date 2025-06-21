package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.HistorialPeso;

import java.time.LocalDate;

public record ObtenerHistorialPesoDTO(
        Integer pesoActual,
        LocalDate fechaActual){
    public ObtenerHistorialPesoDTO(HistorialPeso historial) {
        this(
                historial.getPesoActual(),
                historial.getFechaActual()
        );
    }
}
