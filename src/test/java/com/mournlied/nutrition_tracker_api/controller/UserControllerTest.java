package com.mournlied.nutrition_tracker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mournlied.nutrition_tracker_api.domain.user.dto.*;
import com.mournlied.nutrition_tracker_api.infra.errores.ObjetoRequeridoNoEncontrado;
import com.mournlied.nutrition_tracker_api.infra.errores.TratadorDeErrores;
import com.mournlied.nutrition_tracker_api.infra.errores.ValidacionDeIntegridad;
import com.mournlied.nutrition_tracker_api.infra.security.CustomJwtRoleAndPermissionConverter;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import com.mournlied.nutrition_tracker_api.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({TratadorDeErrores.class, CustomJwtRoleAndPermissionConverter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RolRepository rolRepository;

    @Captor
    private ArgumentCaptor<Authentication> authCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void testCrearUser_valido_ReturnsCreatedResponse() throws Exception {

        UserCreadoDTO creadoDTO = new UserCreadoDTO(
                1L,
                "test@example.com",
                1,
                LocalDate.of(2025,6,3)
        );

        when(userService.crearUser(any(Authentication.class))).thenReturn(creadoDTO);

        mockMvc.perform(post("/api/users")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/users/1"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.estado").value(1))
                .andExpect(jsonPath("$.fechaCreacionUser").value("2025-06-03"));

        verify(userService).crearUser(any(Authentication.class));
    }

    @Test
    @WithMockUser
    void testCrearUser_rolNoExisteEnDB_InternalServerError() throws Exception {

        when(userService.crearUser(any(Authentication.class)))
                .thenThrow(new IllegalStateException("Rol 1L no encontrado"));

        mockMvc.perform(post("/api/users")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testCrearUser_jwtNoValido_InternalServerError() throws Exception {

        when(userService.crearUser(any(Authentication.class)))
                .thenThrow(new SecurityException("Autorización no válida"));

        mockMvc.perform(post("/api/users")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testCrearUser_CorreoYaRegistrado_ReturnsConflict() throws Exception {

        when(userService.crearUser(any(Authentication.class)))
                .thenThrow(new ValidacionDeIntegridad("Correo ya registrado."));

        mockMvc.perform(post("/api/users")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Correo ya registrado."));
    }

    @Test
    @WithMockUser(username = "test.@example.com")
    void testGetUser_userIdExisteEnDB_debeRetornar200YContendioUser() throws Exception{

        ObtenerUserDTO userDTO = new ObtenerUserDTO(
                new InformacionPersonalDTO(
                        new ArrayList<>(),
                        0,
                        "",
                        LocalDate.of(2025,6,4),
                        0,
                        ""),
                "test@example.com",
                1
        );

        when(userService.obtenerUserPorId(any(Long.class),any(Authentication.class)))
                .thenReturn(userDTO);

        mockMvc.perform(get("/api/users/1")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.infoPersonal").isMap())
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.estado").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetUser_idNoPerteneceAUserLogged_debeRetornar403() throws Exception {

        when(userService.obtenerUserPorId(any(Long.class), any(Authentication.class)))
                .thenThrow(new AccessDeniedException("Id no corresponde a la cuenta ingresada actualmente."));

        mockMvc.perform(get("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail")
                        .value("Id no corresponde a la cuenta ingresada actualmente."));

        verify(userService).obtenerUserPorId(eq(1L), authCaptor.capture());
        Authentication authenticacionCapturada = authCaptor.getValue();
        assertEquals("test@example.com", authenticacionCapturada.getName());
    }

    @Test
    @WithMockUser
    void testGetUser_idNoExisteEnDB_debeRetornar404() throws Exception {

        when(userService.obtenerUserPorId(any(Long.class), any(Authentication.class)))
                .thenThrow(new EntityNotFoundException("User no existe."));

        mockMvc.perform(get("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User no existe."));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        verify(userService).obtenerUserPorId(idCaptor.capture(), authCaptor.capture());
        assertEquals(1L, idCaptor.getValue());
    }

    @Test
    @WithMockUser
    void testEliminarUser_userIdExisteEnDB_debeRetornar200() throws Exception{

        doNothing().when(userService).eliminarUser(any(Long.class),any(Authentication.class));

        mockMvc.perform(delete("/api/users/1")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isOk());

        verify(userService).eliminarUser(any(Long.class),any(Authentication.class));
    }

    @Test
    @WithMockUser
    void testEliminarUser_idNoPerteneceAUserLogged_debeRetornar401() throws Exception{

        doThrow(new AccessDeniedException("Id no corresponde a la cuenta ingresada actualmente"))
                .when(userService).eliminarUser(any(Long.class),any(Authentication.class));

        mockMvc.perform(delete("/api/users/1")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail")
                        .value("Id no corresponde a la cuenta ingresada actualmente"));

        verify(userService).eliminarUser(any(Long.class),any(Authentication.class));
    }

    @Test
    @WithMockUser
    void testGetAllUsers_debeRetornar200YPaginaUsers() throws Exception{

        List<ObtenerUserAdminRequestDTO> listaUsers = List.of(new ObtenerUserAdminRequestDTO(
                1L,
                "test@example.com",
                "ADMIN",
                1,
                LocalDate.of(2025,6,7)));

        Page<ObtenerUserAdminRequestDTO> mockPage = new PageImpl<>(
                listaUsers,
                PageRequest.of(0,10),
                listaUsers.size());

        when(userService.obtenerAllUsers(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/users/lista")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].userId").value(1L))
                .andExpect(jsonPath("$.content[0].correo").value("test@example.com"))
                .andExpect(jsonPath("$.content[0].nombreRol").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].estado").value(1))
                .andExpect(jsonPath("$.content[0].fechaCreacionUser").value("2025-06-07"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser
    void testActualizarUser_userIdYRolIdExisten_debeRetornar200YUserActualizado() throws Exception{

        ObtenerUserAdminRequestDTO userDTO = new ObtenerUserAdminRequestDTO(
                1L,
                "test@example.com",
                "ADMIN",
                0,
                LocalDate.of(2025,6,12));

        when(userService.actualizarUser(any(Long.class),any(ActualizarUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/api/users/lista/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rolId\":\"2\",\"estado\":\"0\"}")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.nombreRol").value("ADMIN"))
                .andExpect(jsonPath("$.estado").value(0))
                .andExpect(jsonPath("$.fechaCreacionUser").value("2025-06-12"));
    }

    @Test
    @WithMockUser
    void testActualizarUser_rolIdRequeridoNoExisteEnDB_debeRetornar400() throws Exception{

                when(userService.actualizarUser(any(Long.class),any(ActualizarUserDTO.class)))
                        .thenThrow(new ObjetoRequeridoNoEncontrado("Rol no existe."));

        mockMvc.perform(put("/api/users/lista/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rolId\":\"0\",\"estado\":\"0\"}")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Rol no existe."));
    }

    @Test
    @WithMockUser
    void testActualizarUser_userIdNoExisteEnDB_debeRetornar404() throws Exception{

        when(userService.actualizarUser(any(Long.class),any(ActualizarUserDTO.class)))
                .thenThrow(new EntityNotFoundException("User no existe."));

        mockMvc.perform(put("/api/users/lista/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rolId\":\"0\",\"estado\":\"0\"}")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User no existe."));
    }

    @Test
    @WithMockUser
    void testEliminarUserAdmins_userIdExisteEnDB_debeRetornar200() throws Exception{

        doNothing().when(userService).eliminarUserAdmins(any(Long.class));

        mockMvc.perform(delete("/api/users/lista/1")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isOk());

        verify(userService).eliminarUserAdmins(any(Long.class));
    }

    @Test
    @WithMockUser
    void testEliminarUserAdmins_userIdNoExisteEnDB_debeRetornar400() throws Exception{

        doThrow(new ObjetoRequeridoNoEncontrado("User no existe."))
                .when(userService).eliminarUserAdmins(any(Long.class));

        mockMvc.perform(delete("/api/users/lista/1")
                        .with(csrf())
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("User no existe."));

        verify(userService).eliminarUserAdmins(any(Long.class));
    }
}
