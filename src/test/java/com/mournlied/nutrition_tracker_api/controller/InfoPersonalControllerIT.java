package com.mournlied.nutrition_tracker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ActualizarInfoPersonalBaseDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.RegistroHistorialPesoDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.RegistroInfoPersonalDTO;
import com.mournlied.nutrition_tracker_api.infra.security.SecurityTestConfig;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityTestConfig.class)
@Testcontainers
@Transactional
@Rollback
class InfoPersonalControllerIT {

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
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Jwt testUserJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("email", "admin2@mournlied.com")
            .claim("email_verified", true)
            .build();

    private final Jwt adminJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("email", "admin1@mournlied.com")
            .claim("email_verified", true)
            .build();

    @Test
    void testRegistrarInfoPersonal_requestValida_debeRetornarInfoPersonal() throws Exception{

        RegistroInfoPersonalDTO entradaDTO = new RegistroInfoPersonalDTO(
                80,
                "test test",
                LocalDate.of(1993,4,20),
                185,
                "test objetivos");

        mockMvc.perform(post("/api/personal")
                            .with(jwt().jwt(testUserJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historialPeso").isArray())
                .andExpect(jsonPath("$.pesoInicial").value(80))
                .andExpect(jsonPath("$.nombre").value("test test"));
    }

    @Test
    void testRegistrarInfoPersonal_requestInvalidaDTOEsNull_debeRetornar400() throws Exception{

        RegistroInfoPersonalDTO entradaDTO = null;

        mockMvc.perform(post("/api/personal")
                        .with(jwt().jwt(testUserJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Falta el cuerpo de la solicitud o es inválido."));
    }

    @Test
    void testRegistrarInfoPersonal_requestInvalidaDTOSinCamposObligatoriios_debeRetornar400() throws Exception{

        RegistroInfoPersonalDTO entradaDTO = new RegistroInfoPersonalDTO(
                null,
                "test test",
                null,
                null,
                "test objetivos");

        mockMvc.perform(post("/api/personal")
                        .with(jwt().jwt(testUserJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail[?(@.dato == 'pesoInicial')].error")
                        .value("must not be null"))
                .andExpect(jsonPath("$.detail[?(@.dato == 'nacimiento')].error")
                        .value("must not be null"))
                .andExpect(jsonPath("$.detail[?(@.dato == 'altura')].error")
                        .value("must not be null"));
    }

    @Test
    void testRegistrarInfoPersonal_userNoExiste_debeRetornar404() throws Exception{

        Jwt userInvalido = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", "test@example.com")
                .claim("email_verified", true)
                .build();

        RegistroInfoPersonalDTO entradaDTO = new RegistroInfoPersonalDTO(
                80,
                "test test",
                LocalDate.of(1993,4,20),
                185,
                "test objetivos");

        mockMvc.perform(post("/api/personal")
                            .with(jwt().jwt(userInvalido))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User no existe."));
    }

    @Test
    void testActualizarInfoPersonal_requestValida_debeRetornarInfoPersonalActualizada() throws Exception{

        ActualizarInfoPersonalBaseDTO entradaDTO = new ActualizarInfoPersonalBaseDTO(
                85,
                "nuevo test",
                LocalDate.of(2000,1,1),
                190,
                "nuevo objetivo");

        mockMvc.perform(patch("/api/personal")
                            .with(jwt().jwt(adminJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoInicial").value(85))
                .andExpect(jsonPath("$.nombre").value("nuevo test"))
                .andExpect(jsonPath("$.nacimiento").value("2000-01-01"))
                .andExpect(jsonPath("$.altura").value(190))
                .andExpect(jsonPath("$.objetivos").value("nuevo objetivo"));
    }

    @Test
    void testActualizarInfoPersonal_DTONoContieneTodosLosCmapos_debeActualizarSoloCamposExistentes() throws Exception{

        ActualizarInfoPersonalBaseDTO entradaDTO = new ActualizarInfoPersonalBaseDTO(
                null,
                "nuevo test",
                LocalDate.of(2000,1,1),
                null,
                null);

        mockMvc.perform(patch("/api/personal")
                        .with(jwt().jwt(adminJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoInicial").value(0))
                .andExpect(jsonPath("$.nombre").value("nuevo test"))
                .andExpect(jsonPath("$.nacimiento").value("2000-01-01"))
                .andExpect(jsonPath("$.altura").value(0))
                .andExpect(jsonPath("$.objetivos").value(""));
    }

    @Test
    void testActualizarHistorialPeso_requestValidaSinPaginacion_debeRetornarPaginaPorDefecto() throws Exception{

        RegistroHistorialPesoDTO entradaDTO = new RegistroHistorialPesoDTO(100);

        mockMvc.perform(patch("/api/personal/historial-peso")
                            .with(jwt().jwt(adminJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pesoActual").value(100))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(7))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true));
    }

    @Test
    void testActualizarHistorialPeso_requestValidaConPaginacion_debeRetornarPaginaRequerida() throws Exception{

        RegistroHistorialPesoDTO entradaDTO = new RegistroHistorialPesoDTO(100);

        mockMvc.perform(patch("/api/personal/historial-peso?size=1")
                        .with(jwt().jwt(adminJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pesoActual").value(100))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true));
    }

    @Test
    void testObtenerHistorialPeso_requestValidaYSinPaginacion_debeRetornarPaginaPorDefecto() throws Exception{

        jdbcTemplate.execute("""
                INSERT INTO historial_peso(user_id, peso_registro, fecha_registro)
                SELECT
                    u.user_id,
                    100 - s,
                    CURRENT_DATE - s
                FROM
                    generate_series(0, 9) s,
                    users u
                WHERE u.correo = 'admin1@mournlied.com';""");

        mockMvc.perform(get("/api/personal/historial-peso")
                            .with(jwt().jwt(adminJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(7)))
                .andExpect(jsonPath("$.content[0].pesoActual").value(100))
                .andExpect(jsonPath("$.content[0].fechaActual")
                        .value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.content[1].fechaActual")
                        .value(LocalDate.now().minusDays(1).toString()))
                .andExpect(jsonPath("$.content[6].fechaActual")
                        .value(LocalDate.now().minusDays(6).toString()))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(7))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true));
    }
}