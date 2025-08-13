package com.mournlied.nutrition_tracker_api.controller;

import com.mournlied.nutrition_tracker_api.domain.comida.dto.ActualizarComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ObtenerComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.service.ComidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tracker")
@Validated
@Tag(name = "Seguimiento de comidas",
        description = "Operaciones para administrar entradas de comidas y hacer seguimiento de nutrición")
public class TrackerController {

    private final ComidaService comidaService;

    public TrackerController(ComidaService comidaService){
        this.comidaService = comidaService;
    }

    @PostMapping("/comida")
    @Operation(
            summary = "Registra nueva entrada de comida",
            description = "Crea una nueva entrada de comida para el usuario o la usuaria actual con su respectiva informacion nutricional"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comida registrada satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = ObtenerComidaDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos ingresados inválidos o error de validación"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No Autorizado/a - JWT token inválido o no encontrado"
            )
    })
    public ResponseEntity<ObtenerComidaDTO> registrarNuevaComida(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de entrada de comida",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistroComidaDTO.class))
            )
            @RequestBody @Valid @NotNull RegistroComidaDTO registroComidaDTO){

        return ResponseEntity.ok(comidaService.registrarNuevaComida(jwt, registroComidaDTO));
    }

    @GetMapping("/historial")
    @Operation(
            summary = "Obtener historial de comidas",
            description = "Retorna una lista paginada de comidas dentro de un rango de fechas. Si no se entregan fechas, se retornan los últimos 7 días"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial de comida retornado satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No Autorizado/a - JWT token inválido o no encontrado"
            )
    })
    public ResponseEntity<Page<ObtenerComidaDTO>> obtenerListaComidas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Parametros de paginación")
            @PageableDefault(sort = "fechaCreacionComida", direction = Sort.Direction.DESC) Pageable paginacion,
            @Parameter(description = "Dia inicial para filtro (Formato de fecha ISO: YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Dia final para filtro (Formato de fecha ISO: YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(comidaService.obtenerListaComidas(jwt, paginacion, startDate, endDate));
    }

    @GetMapping("/favoritas")
    @Operation(
            summary = "Obtener comidas favoritas",
            description = "Retorna una lista paginada de las comidas marcadas como favoritas por la usuaria o el usuario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comidas favoritas retornadas satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No Autorizado/a - JWT token inválido o no encontrado"
            )
    })
    public ResponseEntity<Page<ObtenerComidaDTO>> obtenerComidasFavoritas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Parametros de paginación")
            @PageableDefault(sort = "fechaCreacionComida", direction = Sort.Direction.DESC) Pageable paginacion){

        return ResponseEntity.ok(comidaService.obtenerListaComidasFavoritas(jwt, paginacion));
    }

    @PatchMapping("/comida")
    @Operation(
            summary = "Actualizar entrada de comida",
            description = "Actualiza una entrada de comida existente. Solo se actualizan los parametros entregados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entrada de comida actualizada satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = ObtenerComidaDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos ingresados no son válidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La comida objetivo no fue encontrada"
            )
    })
    public ResponseEntity<ObtenerComidaDTO> actualizarComida(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para actualizar en la comida",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ActualizarComidaDTO.class))
            )
            @RequestBody @Valid @NotNull ActualizarComidaDTO actualizarComidaDTO){

        return ResponseEntity.ok(comidaService.actualizarComida(actualizarComidaDTO));
    }

    @DeleteMapping("/comida")
    @Operation(
            summary = "Eliminar entrada de comida",
            description = "Elimina una entrada de comida ingresando su nombre"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entrada de comida eliminada satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nombre de comida inválido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La comida objetivo no fue encontrada"
            )
    })
    public ResponseEntity<Void> eliminarComida(
            @Parameter(description = "Nombre de la comida que se desea eliminar", required = true)
            @RequestParam @NotBlank String nombreComida){

        comidaService.eliminarComida(nombreComida);
        return ResponseEntity.ok().build();
    }
}