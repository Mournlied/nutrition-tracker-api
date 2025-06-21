package com.mournlied.nutrition_tracker_api.infra.security;

import com.mournlied.nutrition_tracker_api.domain.user.Permiso;
import com.mournlied.nutrition_tracker_api.domain.user.Rol;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomJwtRoleAndPermissionConverterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private CustomJwtRoleAndPermissionConverter converter;

    @Test
    void testconvert_correoValidoYExiste_retornaAutoridades(){

        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("correo", "test@example.com")
                .build();
// Rol con 3 autoridades (nombreRol y 2 permisos)
        Rol rol = new Rol(
                1,
                null,
                Set.of(
                        new Permiso(1,null,"LEER_USER_PROPIO"),
                        new Permiso(1,null,"ELIMINAR_USER_PROPIO")),
                Set.of(),null,
                "USER");

        User user = new User(
                null,null,null,rol,null,
                "test@example.com",
                1);

        when(userRepository.findUserWithRolesAndPermisosByCorreo("test@example.com")).thenReturn(Optional.of(user));
        when(rolRepository.findByIdWithPermisosAndHeredados(1)).thenReturn(Optional.of(rol));

        Collection<GrantedAuthority> autoridades = converter.convert(jwt);

        assertThat(autoridades).hasSize(3);
    }

    @Test
    void testconvert_jwtSinCorreo_retornaAutoridadesVacio(){

        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("nombre", "Test Example")
                .build();

        Collection<GrantedAuthority> autoridades = converter.convert(jwt);

        assertThat(autoridades).hasSize(0);
    }

    @Test
    void testconvert_correoNoExisteCorreoEnDB_retornaAutoridadesVacio(){

        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("correo", "test@example.com")
                .build();

        when(userRepository.findUserWithRolesAndPermisosByCorreo("test@example.com")).thenReturn(Optional.empty());


        Collection<GrantedAuthority> autoridades = converter.convert(jwt);

        assertThat(autoridades).hasSize(0);
    }

    @Test
    void testconvert_estadoUserNoEsActivo_retornaAutoridadesVacio(){

        Jwt jwt = Jwt.withTokenValue("dummy")
                .header("alg", "none")
                .claim("correo", "test@example.com")
                .build();

        User user = new User(
                null,null,null,null,null,null,
                0);

        when(userRepository.findUserWithRolesAndPermisosByCorreo("test@example.com")).thenReturn(Optional.of(user));


        Collection<GrantedAuthority> autoridades = converter.convert(jwt);

        assertThat(autoridades).hasSize(0);
    }
}