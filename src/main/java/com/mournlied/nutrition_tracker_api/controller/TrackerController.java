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

        ObtenerComidaDTO nuevaComida = comidaService.registrarNuevaComida(jwt, registroComidaDTO);

        return ResponseEntity.ok(nuevaComida);
    }

    @GetMapping("/historial")
    public ResponseEntity<Page<ObtenerComidaDTO>> obtenerListaComidas(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort = "fechaCreacionComida", direction = Sort.Direction.DESC) Pageable paginacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Page<ObtenerComidaDTO> comidas = comidaService.obtenerListaComidas(jwt, paginacion, startDate, endDate);

        return ResponseEntity.ok(comidas);
    }

    @GetMapping("/favoritas")
    public ResponseEntity<Page<ObtenerComidaDTO>> obtenerComidasFavoritas(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort = "fechaCreacionComida", direction = Sort.Direction.DESC) Pageable paginacion){

        Page<ObtenerComidaDTO> favoritas = comidaService.obtenerListaComidasFavoritas (jwt, paginacion);

        return ResponseEntity.ok(favoritas);
    }

    @PatchMapping("/actualizar-entrada")
    public ResponseEntity<ObtenerComidaDTO> actualizarComida (@RequestBody @Valid ActualizarComidaDTO actualizarComidaDTO){

        ObtenerComidaDTO comidaActualizada = comidaService.actualizarComida(actualizarComidaDTO);

        return  ResponseEntity.ok(comidaActualizada);
    }

    @DeleteMapping("/eliminar-entrada")
    public ResponseEntity<Void> eliminarComida (@RequestBody @NotBlank String nombreComida){

        comidaService.eliminarComida(nombreComida);

        return ResponseEntity.ok().build();
    }
}
