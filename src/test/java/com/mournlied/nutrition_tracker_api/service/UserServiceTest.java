package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.user.Permiso;
import com.mournlied.nutrition_tracker_api.domain.user.Rol;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.domain.user.dto.UserCreadoDTO;
import com.mournlied.nutrition_tracker_api.infra.errores.ValidacionDeIntegridad;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    private final User user = new User("test@example.com");

    private final Rol rol = new Rol(
            1,
            Set.of(user),
            Set.of(new Permiso(1, Collections.emptySet(), "TEST")),
            Collections.emptySet(),
            Collections.emptySet(),
            "ADMIN"
    );

    @BeforeEach
    void setup() {
        when(authentication.getPrincipal()).thenReturn(jwt);
    }

    @Test
    void testCrearUser_requestValida_debeRetornarUserCreado(){

        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(true);
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());
        when(rolRepository.findById(1)).thenReturn(Optional.of(rol));

        UserCreadoDTO salidaDTO = userService.crearUser(authentication);

        assertNotNull(salidaDTO);
        assertEquals("test@example.com", salidaDTO.correo());
        assertEquals(1, salidaDTO.estado());
        assertEquals(LocalDate.now(), salidaDTO.fechaCreacionUser());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCrearUser_correoNoVerificado_debeLanzarExcepcion(){

        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(false);

        assertThrows(SecurityException.class, () -> userService.crearUser(authentication));
    }

    @Test
    void testCrearUser_correoYaRegistrado_debeLanzarExcepcion(){

        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(true);
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(ValidacionDeIntegridad.class, () -> userService.crearUser(authentication));
    }

    @Test
    void testCrearUser_userRolNoExisteEnDB_debeLanzarExcepcion(){

        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(true);
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());
        when(rolRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.crearUser(authentication));
    }
}