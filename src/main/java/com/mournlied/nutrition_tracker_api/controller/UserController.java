package com.mournlied.nutrition_tracker_api.controller;

import com.mournlied.nutrition_tracker_api.domain.user.dto.ActualizarUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserAdminRequestDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.UserCreadoDTO;
import com.mournlied.nutrition_tracker_api.service.UserService;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserCreadoDTO> crearUser(Authentication authentication,
                                                   UriComponentsBuilder uriComponentsBuilder){

        var userCreado = userService.crearUser(authentication);
        URI url = uriComponentsBuilder.path("/users/{id}").buildAndExpand(userCreado.userId()).toUri();
        return ResponseEntity.created(url).body(userCreado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObtenerUserDTO> obtenerUser(@PathVariable Long id, Authentication authentication){

        var userDTO = userService.obtenerUserPorId(id,authentication);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUser(@PathVariable Long id, Authentication authentication){

        userService.eliminarUser(id, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/lista")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ObtenerUserAdminRequestDTO>> obtenerListaUsers(@PageableDefault(size = 10) Pageable paginacion){

        var listaUsers = userService.obtenerAllUsers(paginacion);
        return ResponseEntity.ok().body(listaUsers);
    }

    @PutMapping("/lista/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObtenerUserAdminRequestDTO> actualizarUser (@PathVariable Long id,
                                                                      @RequestBody ActualizarUserDTO actualizarUserDTO){
        var userDTO = userService.actualizarUser(id, actualizarUserDTO);
        return ResponseEntity.ok().body(userDTO);
    }

    @DeleteMapping("/lista/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUserAdmins (@PathVariable Long id){

        userService.eliminarUserAdmins (id);
        return ResponseEntity.ok().build();
    }
}
