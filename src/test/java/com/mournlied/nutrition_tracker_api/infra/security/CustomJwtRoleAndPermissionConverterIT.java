package com.mournlied.nutrition_tracker_api.infra.security;

import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(SecurityTestConfig.class)
@Transactional
public class CustomJwtRoleAndPermissionConverterIT {

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
                .claim("correo", "admin1@mournlied.com")
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