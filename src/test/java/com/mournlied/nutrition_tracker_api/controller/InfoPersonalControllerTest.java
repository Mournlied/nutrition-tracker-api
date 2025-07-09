package com.mournlied.nutrition_tracker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mournlied.nutrition_tracker_api.domain.user.dto.*;
import com.mournlied.nutrition_tracker_api.infra.errores.TratadorDeErrores;
import com.mournlied.nutrition_tracker_api.infra.security.CustomJwtRoleAndPermissionConverter;
import com.mournlied.nutrition_tracker_api.repository.InfoPersonalRepository;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import com.mournlied.nutrition_tracker_api.service.InfoPersonalService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InfoPersonalController.class)
@Import({TratadorDeErrores.class, CustomJwtRoleAndPermissionConverter.class})
class InfoPersonalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    InfoPersonalService personalService;
    @MockitoBean
    InfoPersonalRepository personalRepository;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    RolRepository rolRepository;

    @Captor
    private ArgumentCaptor<Authentication> authCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser
    void testRegistrarInfoPersonal_requestValida_debeRetornar200() throws Exception{

        RegistroInfoPersonalDTO registroDTO = new RegistroInfoPersonalDTO(
                80,
                "test test",
                LocalDate.of(2000,1,1),
                180,
                "test");

        InformacionPersonalDTO salidaDTO = new InformacionPersonalDTO(
                Collections.emptyList(),
                80,
                "test test",
                LocalDate.of(2000,1,1),
                180,
                "test");

        when(personalService.registrarInfoPersonal(any(Jwt.class),any(RegistroInfoPersonalDTO.class)))
                .thenReturn(salidaDTO);

        mockMvc.perform(post("/api/personal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registroDTO))
                            .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historialPeso").isArray())
                .andExpect(jsonPath("$.pesoInicial").value(80))
                .andExpect(jsonPath("$.nombre").value("test test"))
                .andExpect(jsonPath("$.nacimiento").value("2000-01-01"))
                .andExpect(jsonPath("$.altura").value(180))
                .andExpect(jsonPath("$.objetivos").value("test"));

        ArgumentCaptor<RegistroInfoPersonalDTO> registroCaptor = ArgumentCaptor.forClass(RegistroInfoPersonalDTO.class);

        verify(personalService).registrarInfoPersonal(any(Jwt.class), registroCaptor.capture());

        assertEquals(registroDTO, registroCaptor.getValue());
    }

    @Test
    @WithMockUser
    void testRegistrarInfoPersonal_requestInvalida_debeRetornar400() throws Exception{

        RegistroInfoPersonalDTO registroDTO = new RegistroInfoPersonalDTO(
                null,
                "test test",
                null,
                null,
                "test");

        mockMvc.perform(post("/api/personal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registroDTO))
                            .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail[?(@.dato == 'pesoInicial')].error")
                        .value("must not be null"))
                .andExpect(jsonPath("$.detail[?(@.dato == 'nacimiento')].error")
                        .value("must not be null"))
                .andExpect(jsonPath("$.detail[?(@.dato == 'altura')].error")
                        .value("must not be null"));
    }

    @Test
    @WithMockUser
    void testActualizarInfoPersonal_requestValida_debeRetornar200() throws Exception{

        ActualizarInfoPersonalBaseDTO actualizarDTO = new ActualizarInfoPersonalBaseDTO(
                75,
                "test example",
                LocalDate.of(2000,1,10),
                181,
                "nuevo test");

        InformacionPersonalDTO salidaDTO = new InformacionPersonalDTO(
                Collections.emptyList(),
                75,
                "test example",
                LocalDate.of(2000,1,10),
                181,
                "nuevo test");

        when(personalService.actualizarInfoPersonalBase(any(Jwt.class),any(ActualizarInfoPersonalBaseDTO.class)))
                .thenReturn(salidaDTO);

        mockMvc.perform(patch("/api/personal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(actualizarDTO))
                            .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test example"));

        ArgumentCaptor<ActualizarInfoPersonalBaseDTO> dtoCatcher =
                ArgumentCaptor.forClass(ActualizarInfoPersonalBaseDTO.class);

        verify(personalService).actualizarInfoPersonalBase(any(Jwt.class),dtoCatcher.capture());

        assertEquals(actualizarDTO,dtoCatcher.getValue());
    }

    @Test
    @WithMockUser
    void testActualizarInfoPersonal_requestInvalida_debeRetornar400() throws Exception{

        mockMvc.perform(patch("/api/personal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("null")
                            .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Falta el cuerpo de la solicitud o es inválido."));
    }

    @Test
    @WithMockUser
    void testActualizarHistorialPeso_requestValidaSinParams_debeRetornar200() throws Exception{

        RegistroHistorialPesoDTO registroDTO = new RegistroHistorialPesoDTO(79);

        Page<ObtenerHistorialPesoDTO> salida = new PageImpl<>(
                List.of(new ObtenerHistorialPesoDTO(79,LocalDate.of(2025,6,23))),
                PageRequest.of(0,7),
                1);

        when(personalService.actualizarHistorialPeso(
                any(Jwt.class),any(Pageable.class),any(RegistroHistorialPesoDTO.class)))
                .thenReturn(salida);

        mockMvc.perform(patch("/api/personal/historial-peso")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registroDTO))
                            .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pesoActual").value(79))
                .andExpect(jsonPath("$.content[0].fechaActual").value("2025-06-23"));

        ArgumentCaptor<RegistroHistorialPesoDTO> registroCaptor =
                ArgumentCaptor.forClass(RegistroHistorialPesoDTO.class);
        ArgumentCaptor<Pageable> paginacionCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(personalService).actualizarHistorialPeso(
                any(Jwt.class), paginacionCaptor.capture(), registroCaptor.capture());

        assertEquals(PageRequest.of(0,7, Sort.by("fechaActual").descending()),
                paginacionCaptor.getValue());
        assertEquals(registroDTO, registroCaptor.getValue());
    }

    @Test
    @WithMockUser
    void testActualizarHistorialPeso_requestValidaConParams_debeRetornar200() throws Exception{

        RegistroHistorialPesoDTO registroDTO = new RegistroHistorialPesoDTO(79);

        ObtenerHistorialPesoDTO salidaDTO = new ObtenerHistorialPesoDTO(
                79,LocalDate.of(2025,6,23));

        Page<ObtenerHistorialPesoDTO> salida = new PageImpl<>(
                List.of(salidaDTO, salidaDTO, salidaDTO, salidaDTO),
                PageRequest.of(1,2),
                4);

        when(personalService.actualizarHistorialPeso(
                any(Jwt.class),any(Pageable.class),any(RegistroHistorialPesoDTO.class)))
                .thenReturn(salida);

        mockMvc.perform(patch("/api/personal/historial-peso?page=1&size=2&sort=fechaActual,DESC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO))
                        .with(jwt()))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> paginacionCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(personalService).actualizarHistorialPeso(
                any(Jwt.class),paginacionCaptor.capture(),any(RegistroHistorialPesoDTO.class));

        assertEquals(PageRequest.of(1,2, Sort.by("fechaActual").descending()),
                paginacionCaptor.getValue());
    }

    @Test
    @WithMockUser
    void testActualizarHistorialPeso_requestInvlida_debeRetornar400() throws Exception{

        mockMvc.perform(patch("/api/personal/historial-peso")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("null")
                            .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Falta el cuerpo de la solicitud o es inválido."));
    }

    @Test
    @WithMockUser
    void testObtenerHistorialPeso_sinParams_debeRetornar200() throws Exception{

        ObtenerHistorialPesoDTO salidaDTO = new ObtenerHistorialPesoDTO(
                79,LocalDate.of(2025,6,23));

        Page<ObtenerHistorialPesoDTO> salida = new PageImpl<>(
                List.of(salidaDTO, salidaDTO, salidaDTO, salidaDTO),
                PageRequest.of(0,7, Sort.by("fechaActual").descending()),
                4);

        when(personalService.obtenerHistorialPeso(any(Jwt.class),any(Pageable.class))).thenReturn(salida);

        mockMvc.perform(get("/api/personal/historial-peso")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pesoActual").value(79))
                .andExpect(jsonPath("$.content[0].fechaActual").value("2025-06-23"));

        ArgumentCaptor<Pageable> paginacionCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(personalService).obtenerHistorialPeso(
                any(Jwt.class), paginacionCaptor.capture());

        assertEquals(PageRequest.of(0,7, Sort.by("fechaActual").descending()),
                paginacionCaptor.getValue());
    }

    @Test
    @WithMockUser
    void testObtenerHistorialPeso_conParams_debeRetornar200() throws Exception{

        ObtenerHistorialPesoDTO salidaDTO = new ObtenerHistorialPesoDTO(
                79,LocalDate.of(2025,6,23));

        Page<ObtenerHistorialPesoDTO> salida = new PageImpl<>(
                List.of(salidaDTO, salidaDTO, salidaDTO, salidaDTO),
                PageRequest.of(1,2, Sort.by("fechaActual").descending()),
                4);

        when(personalService.obtenerHistorialPeso(any(Jwt.class),any(Pageable.class))).thenReturn(salida);

        mockMvc.perform(get("/api/personal/historial-peso?page=1&size=2")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pesoActual").value(79))
                .andExpect(jsonPath("$.content[0].fechaActual").value("2025-06-23"));

        ArgumentCaptor<Pageable> paginacionCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(personalService).obtenerHistorialPeso(
                any(Jwt.class), paginacionCaptor.capture());

        assertEquals(PageRequest.of(1,2, Sort.by("fechaActual").descending()),
                paginacionCaptor.getValue());
    }
}