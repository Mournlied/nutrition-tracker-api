package com.mournlied.nutrition_tracker_api.controller;

import com.mournlied.nutrition_tracker_api.infra.security.SecurityTestConfig;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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


}