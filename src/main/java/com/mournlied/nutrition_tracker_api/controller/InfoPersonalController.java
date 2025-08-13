package com.mournlied.nutrition_tracker_api.controller;

import com.mournlied.nutrition_tracker_api.domain.user.dto.*;
import com.mournlied.nutrition_tracker_api.service.InfoPersonalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Información Personal",
        description = "Operaciones para administrar la información personal y el historial de peso del usuario o la usuaria")
public class InfoPersonalController {

    private final InfoPersonalService personalService;

    public InfoPersonalController(InfoPersonalService personalService){
        this.personalService = personalService;
    }

    @PostMapping
    @Operation(
            summary = "Registrar información personal",
            description = "Crea la información pesonal inicial para la usuaria o el usuario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Informacion personal registrada satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = InformacionPersonalDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada no válidos o error de validación"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizada/o - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La información personal ya existe para este usuario o esta usuaria"
            )
    })
    public ResponseEntity<InformacionPersonalDTO> registrarInformacionPersonal(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de información personal",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistroInfoPersonalDTO.class))
            )
            @RequestBody @Valid @NotNull RegistroInfoPersonalDTO registroDTO){

        InformacionPersonalDTO infoPersonal = personalService.registrarInfoPersonal(jwt, registroDTO);
        return ResponseEntity.ok(infoPersonal);
    }

    @PatchMapping
    @Operation(
            summary = "Actualizar información personal",
            description = "Actualiza la informacion personal del usuario o la usuaria. Solo los campos ingresados se actualizan"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Informacion personal actualizada satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = InformacionPersonalDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos ingresados no son válidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizada/o - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró información personal para el usuario o la usuaria"
            )
    })
    public ResponseEntity<InformacionPersonalDTO> actualizarInformacionPersonal(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de información personal a acutalizar",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ActualizarInfoPersonalBaseDTO.class))
            )
            @RequestBody @Valid @NotNull ActualizarInfoPersonalBaseDTO actualizarDTO){

        InformacionPersonalDTO infoPersonal = personalService.actualizarInfoPersonalBase(jwt, actualizarDTO);
        return ResponseEntity.ok(infoPersonal);
    }

    @PatchMapping("/historial-peso")
    @Operation(
            summary = "Agregar entrada a historial de peso",
            description = "Agrega una nueva entrada al historial de peso del usuario o la usuaria y retorna una lista paginada del historial de peso"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entrada de peso ingresada satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de peso no válidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizada/o - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró información personal de la usuaria o el usuario"
            )
    })
    public ResponseEntity<Page<ObtenerHistorialPesoDTO>> actualizarHistorialPeso(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Parametros de paginación (defecto: últimas 7 entradas, orden por fecha descendiente)")
            @PageableDefault(size = 7, sort = "fechaActual", direction = Sort.Direction.DESC) Pageable paginacion,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de ingreso de peso",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistroHistorialPesoDTO.class))
            )
            @RequestBody @Valid @NotNull RegistroHistorialPesoDTO registroHistorialPesoDTO){

        Page<ObtenerHistorialPesoDTO> historialPeso = personalService.actualizarHistorialPeso(
                jwt, paginacion, registroHistorialPesoDTO);

        return ResponseEntity.ok(historialPeso);
    }

    @GetMapping("/historial-peso")
    @Operation(
            summary = "Obtener historial de peso",
            description = "Retorna una lista paginada del historial de peso de la usuaria o el usuario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial de peso retornado satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizada/o - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La información personal del usuario o la usuaria no fue encontrada"
            )
    })
    public ResponseEntity<Page<ObtenerHistorialPesoDTO>> obtenerHistorialPeso(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Parametros de paginación (defecto: últimas 7 entradas, orden por fecha descendiente)")
            @PageableDefault(size = 7, sort = "fechaActual", direction = Sort.Direction.DESC) Pageable paginacion){

        return ResponseEntity.ok(personalService.obtenerHistorialPeso(jwt, paginacion));
    }
}