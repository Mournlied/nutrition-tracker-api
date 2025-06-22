package com.mournlied.nutrition_tracker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ActualizarComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ObtenerComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.infra.errores.TratadorDeErrores;
import com.mournlied.nutrition_tracker_api.infra.security.CustomJwtRoleAndPermissionConverter;
import com.mournlied.nutrition_tracker_api.repository.ComidaRepository;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import com.mournlied.nutrition_tracker_api.service.ComidaService;
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
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackerController.class)
@Import({TratadorDeErrores.class, CustomJwtRoleAndPermissionConverter.class})
class TrackerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    ComidaService comidaService;
    @MockitoBean
    ComidaRepository comidaRepository;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    RolRepository rolRepository;

    @Captor
    private ArgumentCaptor<Authentication> authCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void testRegistrarNuevaComida_requestValida_debeRetornar200YComidaCreada() throws Exception {

        RegistroComidaDTO registroDTO = new RegistroComidaDTO(
                "comida test",
                500,
                "descripcion test",
                "Desayuno",
                Map.of("test key","test value"),
                false);

        ObtenerComidaDTO dtoSalida = new ObtenerComidaDTO(
                "comida test",
                LocalDate.of(2025,6,20),
                500,
                "descripcion test",
                "Desayuno",
                Map.of("test key","test value"));

        when(comidaService.registrarNuevaComida(any(Jwt.class),any(RegistroComidaDTO.class))).thenReturn(dtoSalida);

        mockMvc.perform(post("/api/tracker/nueva-entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComida").value("comida test"))
                .andExpect(jsonPath("$.fechaCreacionComida").value("2025-06-20"))
                .andExpect(jsonPath("$.cantidadEnGramos").value(500))
                .andExpect(jsonPath("$.descripcion").value("descripcion test"))
                .andExpect(jsonPath("$.tipoComida").value("Desayuno"))
                .andExpect(jsonPath("$.informacionNutricional").isMap());

        verify(comidaService).registrarNuevaComida(any(Jwt.class),any(RegistroComidaDTO.class));
    }

    @Test
    @WithMockUser
    void testRegistrarNuevaComida_requestInvalida_debeRetornar400() throws Exception {

        RegistroComidaDTO registroDTOinvalido = new RegistroComidaDTO(
                "",
                null,
                "descripcion test",
                null,
                Collections.emptyMap(),
                null);

        mockMvc.perform(post("/api/tracker/nueva-entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTOinvalido))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[?(@.dato == 'informacionNutricional')].error").value(hasItem("must not be empty")))
                .andExpect(jsonPath("$[?(@.dato == 'cantidadEnGramos')].error").value(hasItem("must not be null")))
                .andExpect(jsonPath("$[?(@.dato == 'esFavorita')].error").value(hasItem("must not be null")))
                .andExpect(jsonPath("$[?(@.dato == 'nombreComida')].error").value(hasItem("must not be blank")))
                .andExpect(jsonPath("$[?(@.dato == 'tipoComida')].error").value(hasItem("must not be null")));
    }

    @Test
    void testRegistrarNuevaComida_requestSinAutenticacion_debeRetornar403() throws Exception{

        mockMvc.perform(post("/api/tracker/nueva-entrada"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testObtenerListaComidas_sinParams_debeRetornar200YPaginacionPorDefecto() throws Exception{

        ObtenerComidaDTO comidaDTO = new ObtenerComidaDTO(
                "test",
                LocalDate.of(2025,6,20),
                500,
                "test",
                "test",
                Collections.emptyMap()
        );

        Page<ObtenerComidaDTO> pageDTO = new PageImpl<>(
                List.of(comidaDTO,comidaDTO,comidaDTO),
                PageRequest.of(0,10, Sort.by("fechaCreacionComida").descending()),
                3);

        when(comidaService.obtenerListaComidas(any(Jwt.class),any(Pageable.class),any(),any()))
                .thenReturn(pageDTO);

        mockMvc.perform(get("/api/tracker/historial")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.content[0].nombreComida").value("test"));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(comidaService).obtenerListaComidas(any(Jwt.class), pageableCaptor.capture(), startCaptor.capture(), endCaptor.capture());

        assertNull(startCaptor.getValue());
        assertNull(endCaptor.getValue());
        assertEquals(pageableCaptor.getValue(),PageRequest.of(0,10, Sort.by("fechaCreacionComida").descending()));
    }

    @Test
    @WithMockUser
    void testObtenerListaComidas_conParamsStartDateYEndDate_debeRetornar200YObtenerParametrosCorrectos() throws Exception{

        ObtenerComidaDTO comidaDTO = new ObtenerComidaDTO(
                "test",
                LocalDate.of(2025,6,20),
                500,
                "test",
                "test",
                Collections.emptyMap()
        );

        Page<ObtenerComidaDTO> pageDTO = new PageImpl<>(
                List.of(comidaDTO,comidaDTO,comidaDTO),
                PageRequest.of(0,10, Sort.by("fechaCreacionComida").descending()),
                3);

        when(comidaService.obtenerListaComidas(any(Jwt.class),any(Pageable.class),any(LocalDate.class),any(LocalDate.class)))
                .thenReturn(pageDTO);

        mockMvc.perform(get("/api/tracker/historial?startDate=2025-06-01&endDate=2025-06-08")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(comidaService).obtenerListaComidas(any(Jwt.class), any(Pageable.class), startCaptor.capture(), endCaptor.capture());

        assertEquals(startCaptor.getValue(), LocalDate.of(2025,6,1));
        assertEquals(endCaptor.getValue(), LocalDate.of(2025,6,8));
    }

    @Test
    @WithMockUser
    void testObtenerListaComidas_conPaginacion_debeRetornar200YObtenerPaginacionCorrecta() throws Exception{

        ObtenerComidaDTO comidaDTO = new ObtenerComidaDTO(
                "test",
                LocalDate.of(2025,6,20),
                500,
                "test",
                "test",
                Collections.emptyMap()
        );

        Page<ObtenerComidaDTO> pageDTO = new PageImpl<>(
                List.of(comidaDTO,comidaDTO,comidaDTO),
                PageRequest.of(2,1, Sort.by("cantidadEnGramos").ascending()),
                3);

        when(comidaService.obtenerListaComidas(any(Jwt.class),any(Pageable.class),any(),any()))
                .thenReturn(pageDTO);

        mockMvc.perform(get("/api/tracker/historial?page=2&size=1&sort=cantidadEnGramos,ASC")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(comidaService).obtenerListaComidas(any(Jwt.class), pageableCaptor.capture(), any(), any());

        assertEquals(pageableCaptor.getValue(),PageRequest.of(
                                                    2,
                                                    1,
                                                    Sort.by("cantidadEnGramos").ascending()));
    }

    @Test
    @WithMockUser
    void testObtenerListaComidasFavoritas_sinParams_debeRetornar200YPaginacionPorDefecto() throws Exception{

        ObtenerComidaDTO comidaDTO = new ObtenerComidaDTO(
                "test",
                LocalDate.of(2025,6,20),
                500,
                "test",
                "test",
                Collections.emptyMap()
        );

        Page<ObtenerComidaDTO> pageDTO = new PageImpl<>(
                List.of(comidaDTO,comidaDTO,comidaDTO),
                PageRequest.of(0,10, Sort.by("fechaCreacionComida").descending()),
                3);

        when(comidaService.obtenerListaComidasFavoritas(any(Jwt.class),any(Pageable.class)))
                .thenReturn(pageDTO);

        mockMvc.perform(get("/api/tracker/favoritas")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.content[0].nombreComida").value("test"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(comidaService).obtenerListaComidasFavoritas(any(Jwt.class), pageableCaptor.capture());

        assertEquals(pageableCaptor.getValue(),PageRequest.of(0,10, Sort.by("fechaCreacionComida").descending()));
    }

    @Test
    @WithMockUser
    void testObtenerListaComidasFavoritas_conPaginacion_debeRetornar200YObtenerPaginacionCorrecta() throws Exception{

        ObtenerComidaDTO comidaDTO = new ObtenerComidaDTO(
                "test",
                LocalDate.of(2025,6,20),
                500,
                "test",
                "test",
                Collections.emptyMap()
        );

        Page<ObtenerComidaDTO> pageDTO = new PageImpl<>(
                List.of(comidaDTO,comidaDTO,comidaDTO),
                PageRequest.of(2,1, Sort.by("nombreComida").ascending()),
                3);

        when(comidaService.obtenerListaComidasFavoritas(any(Jwt.class),any(Pageable.class)))
                .thenReturn(pageDTO);

        mockMvc.perform(get("/api/tracker/favoritas?page=2&size=1&sort=nombreComida,ASC")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(comidaService).obtenerListaComidasFavoritas(any(Jwt.class), pageableCaptor.capture());

        assertEquals(pageableCaptor.getValue(),PageRequest.of(2,1, Sort.by("nombreComida").ascending()));
    }

    @Test
    @WithMockUser
    void testActualizarComida_requestValida_debeRetornar200YComidaActualizada() throws Exception{

        ActualizarComidaDTO requestDTO = new ActualizarComidaDTO(
                "original test",
                "test nuevo",
                500,
                "test",
                "test",
                Map.of("test key","test value"),
                true);

        ObtenerComidaDTO dtoSalida = new ObtenerComidaDTO(
                "test nuevo",
                LocalDate.of(2025,6,20),
                500,
                "test",
                "test",
                Map.of("test key","test value"));

        when(comidaService.actualizarComida(requestDTO)).thenReturn(dtoSalida);

        mockMvc.perform(patch("/api/tracker/actualizar-entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComida").value("test nuevo"))
                .andExpect(jsonPath("$.informacionNutricional").isMap());

        verify(comidaService).actualizarComida(requestDTO);
    }

    @Test
    @WithMockUser
    void testActualizarComida_requestInvalida_debeRetornar400() throws Exception{

        ActualizarComidaDTO requestDTO = new ActualizarComidaDTO(
                "",
                "test nuevo",
                500,
                "test",
                "test",
                Map.of("test key","test value"),
                true);

        mockMvc.perform(patch("/api/tracker/actualizar-entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[?(@.dato == 'nombreComidaOriginal')].error").value("must not be blank"));
    }

    @Test
    @WithMockUser
    void testEliminarComida_requestValida_debeRetornar200() throws Exception{

        mockMvc.perform(delete("/api/tracker/eliminar-entrada?nombreComida=test")
                            .with(jwt()))
                .andExpect(status().isOk());

        ArgumentCaptor<String> nombreCaptor = ArgumentCaptor.forClass(String.class);

        verify(comidaService).eliminarComida(nombreCaptor.capture());

        assertEquals("test", nombreCaptor.getValue());
    }

    @Test
    @WithMockUser
    void testEliminarComida_requestInvalida_debeRetornar400() throws Exception{

        mockMvc.perform(delete("/api/tracker/eliminar-entrada?nombreComida=")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("eliminarComida.nombreComida: must not be blank"));
    }
}