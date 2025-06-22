package com.mournlied.nutrition_tracker_api.controller;


import com.mournlied.nutrition_tracker_api.domain.comida.dto.ActualizarComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ObtenerComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.service.ComidaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tracker")
public class TrackerController {

    private final ComidaService comidaService;

    public TrackerController(ComidaService comidaService){
        this.comidaService = comidaService;
    }

    @PostMapping("/nueva-entrada")
    public ResponseEntity<ObtenerComidaDTO> registrarNuevaComida(
            @AuthenticationPrincipal Jwt jwt, @RequestBody @Valid RegistroComidaDTO registroComidaDTO){

        return ResponseEntity.ok(comidaService.registrarNuevaComida(jwt, registroComidaDTO));
    }

    @GetMapping("/historial")
    public ResponseEntity<Page<ObtenerComidaDTO>> obtenerListaComidas(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort = "fechaCreacionComida", direction = Sort.Direction.DESC) Pageable paginacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(comidaService.obtenerListaComidas(jwt, paginacion, startDate, endDate));
    }

    @GetMapping("/favoritas")
    public ResponseEntity<Page<ObtenerComidaDTO>> obtenerComidasFavoritas(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort = "fechaCreacionComida", direction = Sort.Direction.DESC) Pageable paginacion){

        return ResponseEntity.ok(comidaService.obtenerListaComidasFavoritas (jwt, paginacion));
    }

    @PatchMapping("/actualizar-entrada")
    public ResponseEntity<ObtenerComidaDTO> actualizarComida (@RequestBody @Valid ActualizarComidaDTO actualizarComidaDTO){

        return  ResponseEntity.ok(comidaService.actualizarComida(actualizarComidaDTO));
    }

    @DeleteMapping("/eliminar-entrada")
    public ResponseEntity<Void> eliminarComida (@RequestParam @NotBlank String nombreComida){

        comidaService.eliminarComida(nombreComida);

        return ResponseEntity.ok().build();
    }
}
