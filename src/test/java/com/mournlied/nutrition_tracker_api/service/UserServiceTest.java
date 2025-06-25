package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.user.*;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ActualizarUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserAdminRequestDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.UserCreadoDTO;
import com.mournlied.nutrition_tracker_api.infra.errores.ObjetoRequeridoNoEncontrado;
import com.mournlied.nutrition_tracker_api.infra.errores.ValidacionDeIntegridad;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private final User userCreacion = new User("test@example.com");

    private final Rol userRol = new Rol(
            1,
            Set.of(userCreacion),
            Set.of(new Permiso(1, Collections.emptySet(), "TEST")),
            Collections.emptySet(),
            Collections.emptySet(),
            "USER");

    private final InformacionPersonal personal = new InformacionPersonal(
            1L,
            new User(),
            List.of(new HistorialPeso()),
            80,
            "test test",
            LocalDate.of(2000,1,1),
            180,
            "test");

    private final User userDB = new User(
            1L,
            Collections.emptyList(),
            personal,
            userRol,
            LocalDate.of(2025,6,20),
            "test@example.com",
            1);

    @BeforeEach
    void setup() {
        lenient().when(authentication.getPrincipal()).thenReturn(jwt);
    }

    @Test
    void testCrearUser_requestValida_debeRetornarUserCreado(){

        when(jwt.getClaimAsBoolean("email_verified")).thenReturn(true);
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());
        when(rolRepository.findById(1)).thenReturn(Optional.of(userRol));

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
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userCreacion));

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

    @Test
    void testObtenerUserPorId_requestValida_debeRetornarUserBuscado(){

        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));

        ObtenerUserDTO salidaDTO = userService.obtenerUserPorId(1L, authentication);

        assertEquals("test@example.com", salidaDTO.correo());
        assertEquals(1,salidaDTO.estado());
    }

    @Test
    void testObtenerUserPorId_userIdNoRegistrada_debeLanzarExcepcion(){

        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ObjetoRequeridoNoEncontrado.class, () -> userService.obtenerUserPorId(2L, authentication));
    }

    @Test
    void testObtenerUserPorId_idParamNoCoincideConAutenticacion_debeLanzarExcepcion(){

        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));

        assertThrows(AccessDeniedException.class, () -> userService.obtenerUserPorId(2L, authentication));
    }

    @Test
    void testeliminarUser_userExiste_debeEliminarUser(){

        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));

        userService.eliminarUser(1L, authentication);

        verify(userRepository).delete(any(User.class));
    }

    @Test
    void testObtenerAllUsers_debeRetornarPaginaUsers(){

        Pageable paginacion = PageRequest.of(0,10, Sort.by("userId").ascending());

        Page<User> paginaUsers = new PageImpl<>(
                List.of(userDB, userDB),
                paginacion,
                2
        );

        when(userRepository.findAll(paginacion)).thenReturn(paginaUsers);

        Page<ObtenerUserAdminRequestDTO> paginaSalida = userService.obtenerAllUsers(paginacion);

        assertEquals(new ObtenerUserAdminRequestDTO(userDB), paginaSalida.getContent().get(0));
        assertEquals(2, paginaSalida.getContent().size());
        assertEquals(paginacion.getPageSize(), paginaSalida.getPageable().getPageSize());
    }

    @Test
    void testActualizarUser_DTOContieneRolValidoYEstado_debeActualizarRolYEstado(){

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(2, 2);

        Rol adminRol = new Rol(
                2,
                Set.of(userCreacion),
                Set.of(new Permiso(2, Collections.emptySet(), "TEST2")),
                Collections.emptySet(),
                Collections.emptySet(),
                "ADMIN");

        when(rolRepository.findRolByRolId(2)).thenReturn(Optional.of(adminRol));
        when(userRepository.findUserByUserId(1L)).thenReturn(Optional.of(userDB));

        ObtenerUserAdminRequestDTO salidaDTO = userService.actualizarUser(1L, entradaDTO);

        assertEquals(2, salidaDTO.estado());
        assertEquals("ADMIN", salidaDTO.nombreRol());
    }

    @Test
    void testActualizarUser_DTOContieneSoloRolValido_debeActualizarRolYEstado(){

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(2, null);

        Rol adminRol = new Rol(
                2,
                Set.of(userCreacion),
                Set.of(new Permiso(2, Collections.emptySet(), "TEST2")),
                Collections.emptySet(),
                Collections.emptySet(),
                "ADMIN");

        when(rolRepository.findRolByRolId(2)).thenReturn(Optional.of(adminRol));
        when(userRepository.findUserByUserId(1L)).thenReturn(Optional.of(userDB));

        ObtenerUserAdminRequestDTO salidaDTO = userService.actualizarUser(1L, entradaDTO);

        assertEquals(1, salidaDTO.estado());
        assertEquals("ADMIN", salidaDTO.nombreRol());
    }

    @Test
    void testActualizarUser_DTOContieneSoloEstado_debeActualizarRolYEstado(){

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(null, 2);

        when(userRepository.findUserByUserId(1L)).thenReturn(Optional.of(userDB));

        ObtenerUserAdminRequestDTO salidaDTO = userService.actualizarUser(1L, entradaDTO);

        assertEquals(2, salidaDTO.estado());
        assertEquals("USER", salidaDTO.nombreRol());
    }

    @Test
    void testActualizarUser_rolIdNoExisteEnDb_debeLanzarExcepcion(){

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(2, 2);

        when(userRepository.findUserByUserId(1L)).thenReturn(Optional.of(userDB));
        when(rolRepository.findRolByRolId(2)).thenReturn(Optional.empty());

        assertThrows(ObjetoRequeridoNoEncontrado.class, () -> userService.actualizarUser(1L, entradaDTO));
    }

    @Test
    void testEliminarUserAdmin_UserExiste_debeEliminarUser(){

        when(userRepository.findUserByUserId(1L)).thenReturn(Optional.of(userDB));

        userService.eliminarUserAdmins(1L);

        verify(userRepository).delete(any(User.class));
    }

    @Test
    void testHelper_UserNoExiste_debeLanzarExcepcion(){

        when(userRepository.findUserByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(ObjetoRequeridoNoEncontrado.class, () -> userService.eliminarUserAdmins(2L));
    }
}