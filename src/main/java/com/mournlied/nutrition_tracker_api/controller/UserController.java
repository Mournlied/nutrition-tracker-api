package com.mournlied.nutrition_tracker_api.controller;

import com.mournlied.nutrition_tracker_api.domain.user.dto.ActualizarUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserAdminRequestDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.UserCreadoDTO;
import com.mournlied.nutrition_tracker_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Administración de users",
        description = "Operaciones para administrar las cuentas de las usuarias y los usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @Operation(
            summary = "Crear nueva cuenta de usuario o usuaria",
            description = "Crea una nueva cuenta desde token entregado por Auth0. Correo debe estar verificado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User creado o creada satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = UserCreadoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado/a - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe user con el correo ingresado"
            )
    })
    public ResponseEntity<UserCreadoDTO> crearUser(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) UriComponentsBuilder uriComponentsBuilder){

        var userCreado = userService.crearUser(authentication);
        URI url = uriComponentsBuilder.path("/users/{id}").buildAndExpand(userCreado.userId()).toUri();
        return ResponseEntity.created(url).body(userCreado);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener user por ID",
            description = "Retorna la informacion del usuario o la usuaria del ID ingresado. Solo se puede obtener la información propia a menos que posea privilegios de Admin"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retornado/a satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = ObtenerUserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado/a - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado, el usuario o la usuaria solo pueden acceder a su propia información"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User no encontrada/o"
            )
    })
    public ResponseEntity<ObtenerUserDTO> obtenerUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication){

        var userDTO = userService.obtenerUserPorId(id, authentication);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar cuenta",
            description = "Elimina una cuenta de usuaria/o. Solo se puede elmiminar la cuenta propia a menos que posea privilegios de Admin"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User eliminada/o satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado/a - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado, la usuaria o el usuario solo pueden eliminar su propia cuenta"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User no encontrado/a"
            )
    })
    public ResponseEntity<Void> eliminarUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication){

        userService.eliminarUser(id, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/lista")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Obtener lista de users (Solo Admins)",
            description = "Retorna la lista completa de usuarios y usuarias. Requiere rol de Administrador/a",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado/a - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado, se requiere el rol de Admin"
            )
    })
    public ResponseEntity<Page<ObtenerUserAdminRequestDTO>> obtenerListaUsers(
            @Parameter(description = "Parametros de paginación (defecto: 10 entradas)")
            @PageableDefault(size = 10) Pageable paginacion){

        var listaUsers = userService.obtenerAllUsers(paginacion);
        return ResponseEntity.ok().body(listaUsers);
    }

    @PutMapping("/lista/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar user (Solo Admins)",
            description = "Actualiza rol o estado del usuario o la usuaria. Requiere rol de administrador/a",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuaria/o actualizada/o satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = ObtenerUserAdminRequestDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos entregados no son válidos - Rol objetivo no existe"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado/a - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado, se requiere el rol de Admin"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario/a no encontrado/a"
            )
    })
    public ResponseEntity<ObtenerUserAdminRequestDTO> actualizarUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de user a acutalizar",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ActualizarUserDTO.class))
            )
            @RequestBody ActualizarUserDTO actualizarUserDTO){

        var userDTO = userService.actualizarUser(id, actualizarUserDTO);
        return ResponseEntity.ok().body(userDTO);
    }

    @DeleteMapping("/lista/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar usuaria/o (Solo Admins)",
            description = "Elimina cualquier cuenta. Requiere rol de administrador/a",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario/a eliminado/a satisfactoriamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado/a - JWT token inválido o no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado, se requiere el rol de Admin"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuaria/o no encontrada/o"
            )
    })
    public ResponseEntity<Void> eliminarUserAdmins(
            @Parameter(description = "User ID", required = true) @PathVariable Long id){

        userService.eliminarUserAdmins(id);
        return ResponseEntity.ok().build();
    }
}