package com.mournlied.nutrition_tracker_api.infra.security;

import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(SecurityTestConfig.class)
@Testcontainers
@Transactional
@Rollback
public class CustomJwtRoleAndPermissionConverterIT {

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
    private UserRepository userRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private CustomJwtRoleAndPermissionConverter converter;

    @Test
    void testCargaRecursivaDeRolesEnJerarquiaYPermisos() {

        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("email", "admin1@mournlied.com")
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        Set<String> nombresAutoridades = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(nombresAutoridades).contains("ROLE_ADMIN", "ROLE_USER");
        assertThat(nombresAutoridades).hasSize(9);
        assertThat(nombresAutoridades).contains(
                "PERM_LEER_USER_PROPIO"
                , "PERM_MODIFICAR_INFO_PERSONAL"
                , "PERM_ELIMINAR_USER_PROPIO");
    }
}