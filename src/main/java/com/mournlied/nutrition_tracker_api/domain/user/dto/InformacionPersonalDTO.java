package com.mournlied.nutrition_tracker_api.domain.user.dto;

import com.mournlied.nutrition_tracker_api.domain.user.InformacionPersonal;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record InformacionPersonalDTO (
   List<ObtenerHistorialPesoDTO> historialPeso,
   Integer pesoInicial,
   String nombre,
   LocalDate nacimiento,
   Integer altura,
   String objetivos){
    public InformacionPersonalDTO(InformacionPersonal infoPersonal) {
        this(infoPersonal.getHistorialPeso().stream().map(ObtenerHistorialPesoDTO::new).collect(Collectors.toList()),
                infoPersonal.getPesoInicial(),
                infoPersonal.getNombre(),
                infoPersonal.getNacimiento(),
                infoPersonal.getAltura(),
                infoPersonal.getObjetivos());
    }
}
