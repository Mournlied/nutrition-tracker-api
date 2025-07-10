package com.mournlied.nutrition_tracker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ActualizarComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.infra.security.SecurityTestConfig;
import com.mournlied.nutrition_tracker_api.repository.ComidaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityTestConfig.class)
@Testcontainers
@Transactional
@Rollback
class TrackerControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ComidaRepository comidaRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    //Migracion de test V9003 crea una comida por dia para los ultimos 9 dias incluyendo hoy.
    @Test
    void testRegistrarNuevaComida_requestValida_debeRetornarComidaCreada() throws Exception{

        RegistroComidaDTO entradaDTO = new RegistroComidaDTO(
                "test test",
                500,
                "test",
                "Snack",
                Map.of("calorias",420, "proteinas", 69, "carbohidratos", Map.of("totales",69)),
                true
        );

        mockMvc.perform(post("/api/tracker/comida")
                            .header("Authorization", "Bearer mock-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComida").value("test test"))
                .andExpect(jsonPath("$.cantidadEnGramos").value(500))
                .andExpect(jsonPath("$.informacionNutricional").isMap());
    }

    @Test
    void testRegistrarNuevaComida_requestDTOEsNull_debeRetornar401() throws Exception{

        RegistroComidaDTO entradaDTO = null;

        mockMvc.perform(post("/api/tracker/comida")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Falta el cuerpo de la solicitud o es inválido."));
    }

    @Test
    void testRegistrarNuevaComida_nombreComidaYaExiste_debeRetornar400() throws Exception{

        RegistroComidaDTO entradaDTO = new RegistroComidaDTO(
                "comida numero 1",
                500,
                "test",
                "Snack",
                Map.of("calorias",420,
                        "proteinas", 69,
                        "carbohidratos", Map.of("totales",69)),
                true
        );

        mockMvc.perform(post("/api/tracker/comida")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Ya existe una comida registrada con ese nombre."));
    }

    @Test
    void testRegistrarNuevaComida_userAutenticadoNoExiste_debeRetornar404() throws Exception{

        Jwt testUserJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", "test@example.com")
                .claim("email_verified", true)
                .build();

        RegistroComidaDTO entradaDTO = new RegistroComidaDTO(
                "comida numero 1",
                500,
                "test",
                "Snack",
                Map.of("calorias",420,
                        "proteinas", 69,
                        "carbohidratos", Map.of("totales",69)),
                true
        );

        mockMvc.perform(post("/api/tracker/comida")
                            .with(jwt().jwt(testUserJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User no existe."));
    }

    @Test
    void testObtenerListaComidas_requestValidaYSinPaginacion_debeRetornarPaginaPorDefecto() throws Exception{

        mockMvc.perform(get("/api/tracker/historial")
                            .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(7)))//Sin start/endDate solo ultimos 7 dias
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.content[0].fechaCreacionComida")
                        .value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.content[1].fechaCreacionComida")
                        .value(LocalDate.now().minusDays(1).toString()));

        assertEquals(9, comidaRepository.findAll().size());
    }

    @Test
    void testObtenerListaComidas_requestValidaConPaginacionYParams_debeRetornarPaginaRequerida() throws Exception{

        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().minusDays(1);
        String uriDateParams = "&startDate="+ startDate+ "&endDate="+ endDate;

        mockMvc.perform(get("/api/tracker/historial?size=4&sort=fechaCreacionComida,ASC"+ uriDateParams)
                            .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.content[0].fechaCreacionComida")
                        .value(LocalDate.now().minusDays(5).toString()))
                .andExpect(jsonPath("$.content[3].fechaCreacionComida")
                        .value(LocalDate.now().minusDays(2).toString()));
    }

    @Test
    void testObtenerListaComidas_SortIncorrecto_debeRetornar400() throws Exception{

        mockMvc.perform(get("/api/tracker/historial?sort=fecha")
                            .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Uno de los campos proporcionados no es válido."));
    }

    @Test
    void testObtenerListaComidasFavoritas_requestValidaYSinPaginacion_debeRetornarPaginaPorDefecto() throws Exception{

        mockMvc.perform(get("/api/tracker/favoritas")
                            .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true));
    }

    @Test
    void testObtenerListaComidasFavoritas_requestInvalidaPost_debeRetornar409() throws Exception {

        mockMvc.perform(post("/api/tracker/favoritas")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.detail").value("Method 'POST' is not supported."));
    }

    @Test
    void testActualizarComida_requestValida_debeRetornarComidaActualizada() throws Exception {

        ActualizarComidaDTO entradaDTO = new ActualizarComidaDTO(
                "comida numero 1",
                "comida numero 0",
                8000,
                "nueva descripcion",
                "Otra",
                null,
                true
        );

        mockMvc.perform(patch("/api/tracker/comida")
                            .header("Authorization", "Bearer mock-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComida").value("comida numero 0"))
                .andExpect(jsonPath("$.cantidadEnGramos").value(8000))
                .andExpect(jsonPath("$.descripcion").value("nueva descripcion"))
                .andExpect(jsonPath("$.tipoComida").value("Otra"));
    }

    @Test
    void testActualizarComida_requestInvalidaDTOEsNull_debeRetornar400() throws Exception {

        ActualizarComidaDTO entradaDTO = null;

        mockMvc.perform(patch("/api/tracker/comida")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Falta el cuerpo de la solicitud o es inválido."));
    }

    @Test
    void testActualizarComida_requestInvalidaNombreComidaOriginalNoExiste_debeRetornar400() throws Exception {

        ActualizarComidaDTO entradaDTO = new ActualizarComidaDTO(
                "test",
                "comida numero 0",
                8000,
                "nueva descripcion",
                "Otra",
                null,
                true
        );

        mockMvc.perform(patch("/api/tracker/comida")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Comida no existe."));
    }

    @Test
    void testEliminarComida_requestValida_debeRetornar200() throws Exception{

        String nombreComida = "comida numero 1";

        mockMvc.perform(delete("/api/tracker/comida")
                            .param("nombreComida", nombreComida)
                            .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk());

        assertEquals(Optional.empty(), comidaRepository.findByNombreComida("comida numero 1"));
    }

    @Test
    void testEliminarComida_requestInvalidaSinParam_debeRetornar400() throws Exception{

        mockMvc.perform(delete("/api/tracker/comida")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Required parameter 'nombreComida' is not present."));
    }
}