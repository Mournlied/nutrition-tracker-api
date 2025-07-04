package com.mournlied.nutrition_tracker_api.controller;

import com.mournlied.nutrition_tracker_api.domain.user.dto.*;
import com.mournlied.nutrition_tracker_api.service.InfoPersonalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personal")
@Validated
public class InfoPersonalController {

    private final InfoPersonalService personalService;

    public InfoPersonalController(InfoPersonalService personalService){
        this.personalService = personalService;
    }

    @PostMapping
    public ResponseEntity<InformacionPersonalDTO> registrarInformacionPersonal(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid @NotNull RegistroInfoPersonalDTO registroDTO){

        InformacionPersonalDTO infoPersonal = personalService.registrarInfoPersonal(jwt, registroDTO);

        return ResponseEntity.ok(infoPersonal);
    }

    @PatchMapping
    public ResponseEntity<InformacionPersonalDTO> actualizarInformacionPersonal(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid @NotNull ActualizarInfoPersonalBaseDTO actualizarDTO){

        InformacionPersonalDTO infoPersonal = personalService.actualizarInfoPersonalBase(jwt, actualizarDTO);

        return  ResponseEntity.ok(infoPersonal);
    }

    @PatchMapping("/historial-peso")
    public ResponseEntity<Page<ObtenerHistorialPesoDTO>> actualizarHistorialPeso(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 7, sort = "fechaActual", direction = Sort.Direction.DESC) Pageable paginacion,
            @RequestBody @Valid @NotNull RegistroHistorialPesoDTO registroHistorialPesoDTO){

        Page<ObtenerHistorialPesoDTO> historialPeso = personalService.actualizarHistorialPeso(
                jwt, paginacion, registroHistorialPesoDTO);

        return ResponseEntity.ok(historialPeso);
    }

    @GetMapping("/historial-peso")
    public ResponseEntity<Page<ObtenerHistorialPesoDTO>> obtenerHistorialPeso(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 7, sort = "fechaActual", direction = Sort.Direction.DESC) Pageable paginacion){

        return ResponseEntity.ok(personalService.obtenerHistorialPeso(jwt,paginacion));
    }
}
