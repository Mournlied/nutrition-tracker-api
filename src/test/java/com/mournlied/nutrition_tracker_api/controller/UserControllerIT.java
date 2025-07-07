package com.mournlied.nutrition_tracker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ActualizarUserDTO;
import com.mournlied.nutrition_tracker_api.infra.security.SecurityTestConfig;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
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
class UserControllerIT {

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
    @Autowired
    private UserRepository userRepository;

    private final Jwt adminJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("email", "admin1@mournlied.com")
            .claim("email_verified", true)
            .build();

    private final Jwt testUserJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("email", "user1@mournlied.com")
            .claim("email_verified", true)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testCrearUser_sinJwt_debeRetornar401() throws Exception{

        mockMvc.perform(post("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCrearUser_requestValida_debeRetornarUserCreado() throws Exception {

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", "test@example.com")
                .claim("email_verified", true)
                .build();

        mockMvc.perform(post("/api/users")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesRegex(".*/users/\\d+")))
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.estado").value(1));

        assertNotNull(userRepository.findUserByCorreo("test@example.com"));
    }

    @Test
    void testCrearUser_correoNoVerificado_debeRetornar400() throws Exception {

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", "test@example.com")
                .claim("email_verified", false)
                .build();

        mockMvc.perform(post("/api/users")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Primero debe verificar su correo"));
    }

    @Test
    void testCrearUser_correoYaExistente_debeRetornar401() throws Exception {

        mockMvc.perform(post("/api/users")
                        .with(jwt().jwt(adminJwt)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Correo ya registrado"));
    }

    @Test
    void testCrearUser_rolNoExiste_debeRetornar500() throws Exception{

        jdbcTemplate.execute("DELETE FROM rol_hierarchy WHERE rol_hijo_id = 1 OR rol_padre_id = 1");
        jdbcTemplate.execute("DELETE FROM rol_permisos WHERE rol_id = 1");
        jdbcTemplate.execute("DELETE FROM users WHERE rol_id = 1");
        jdbcTemplate.execute("DELETE FROM roles WHERE rol_id = 1");

        mockMvc.perform(post("/api/users")
                        .with(jwt().jwt(testUserJwt)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Rol 1L no encontrado"));
    }

    @Test
    void testObtenerUser_requestValida_debeRetornarUser() throws Exception{

        mockMvc.perform(get("/api/users/1")
                            .with(jwt().jwt(adminJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(1))
                .andExpect(jsonPath("$.correo").value("admin1@mournlied.com"));
    }

    @Test
    void testObtenerUser_userAutenticadoNoRegistrado_debeRetornar400() throws Exception{

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", "test@example.com")
                .claim("email_verified", true)
                .build();

        mockMvc.perform(get("/api/users/1")
                            .with(jwt().jwt(jwt)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User no registrada/o"));
    }

    @Test
    void testObtenerUser_idNoCorrespondeAUserAutenticado_debeRetornar403() throws Exception{

        mockMvc.perform(get("/api/users/1")
                            .with(jwt().jwt(testUserJwt)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Id no corresponde a la cuenta ingresada actualmente"));
    }

    @Test
    void testEliminarUser_requestValida_debeRetornar200() throws Exception{

        mockMvc.perform(delete("/api/users/2")
                            .with(jwt().jwt(testUserJwt)))
                .andExpect(status().isOk());

        assertEquals(Optional.empty() ,userRepository.findUserByCorreo("user1@mournlied.com"));
    }

    @Test
    void testObtenerListaUsers_userEsAdminYSinPaginacion_debeRetornarPaginaPorDefecto() throws Exception{

        mockMvc.perform(get("/api/users/lista")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    void testObtenerListaUsers_userEsAdminYConPaginacion_debeRetornarPaginaPorDefecto() throws Exception {

        mockMvc.perform(get("/api/users/lista?size=6&page=0&sort=correo,ASC")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].correo").value("admin1@mournlied.com"))
                .andExpect(jsonPath("$.content[5].correo").value("user1@mournlied.com"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(6))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true));
    }

    @Test
    void testObtenerListaUsers_userNoEsAdmin_debeRetornar403() throws Exception{

        mockMvc.perform(get("/api/users/lista")
                            .with(jwt().jwt(testUserJwt)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access Denied"));
    }

    @Test
    void testObtenerActualizarUser_userEsAdminYRequestValida_debeRetornar200() throws Exception{

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(2,2);

        mockMvc.perform(put("/api/users/lista/2")
                            .header("Authorization", "Bearer mock-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("user1@mournlied.com"))
                .andExpect(jsonPath("$.estado").value(2))
                .andExpect(jsonPath("$.nombreRol").value("ADMIN"));
    }

    @Test
    void testObtenerActualizarUser_userEsAdminYRequestSinBody_debeRetornar400() throws Exception{

        ActualizarUserDTO entradaDTO = null;

        mockMvc.perform(put("/api/users/lista/2")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Required request body is missing"));
    }

    @Test
    void testObtenerActualizarUser_userEsAdminYBodyContieneValoresNulos_debeRetornarUserSinCambios() throws Exception{

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(null,null);

        mockMvc.perform(put("/api/users/lista/2")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("user1@mournlied.com"))
                .andExpect(jsonPath("$.estado").value(1))
                .andExpect(jsonPath("$.nombreRol").value("USER"));
    }

    @Test
    void testObtenerActualizarUser_userNoEsAdmin_debeRetornar403() throws Exception{

        ActualizarUserDTO entradaDTO = new ActualizarUserDTO(2,2);

        mockMvc.perform(put("/api/users/lista/2")
                        .with(jwt().jwt(testUserJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access Denied"));
    }

    @Test
    void testEliminarUserAdmin_userEsAdmin_debeRetornar200() throws Exception{

        mockMvc.perform(delete("/api/users/lista/2")
                            .header("Authorization", "Bearer mock-tocken"))
                .andExpect(status().isOk());

        assertEquals(Optional.empty(), userRepository.findUserByCorreo("user1@mournlied.com"));
    }

    @Test
    void testEliminarUserAdmin_userNoEsAdmin_debeRetornar403() throws Exception{

        mockMvc.perform(delete("/api/users/lista/2")
                            .with(jwt().jwt(testUserJwt)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access Denied"));
    }
}