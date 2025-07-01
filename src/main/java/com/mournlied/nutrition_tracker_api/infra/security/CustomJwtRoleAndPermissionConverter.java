package com.mournlied.nutrition_tracker_api.infra.security;

import com.mournlied.nutrition_tracker_api.domain.user.Permiso;
import com.mournlied.nutrition_tracker_api.domain.user.Rol;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomJwtRoleAndPermissionConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;

    public CustomJwtRoleAndPermissionConverter(UserRepository userRepository, RolRepository rolRepository) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
    }

    private Set<Rol> obtenerJerarquiaCompleta(Rol inicial) {
        Set<Rol> rolesEnJerarquia = new HashSet<>();
        Deque<Rol> stack = new ArrayDeque<>();
        stack.push(inicial);

        while (!stack.isEmpty()) {
            Rol actual = stack.pop();
            if (rolesEnJerarquia.add(actual)) {
                Integer id = actual.getRolId();
                Optional<Rol> padre = rolRepository.findByIdWithPermisosAndHeredados(id);
                if (padre.isPresent()) {
                    for (Rol hijo : padre.get().getRolesHeredados()) {
                        stack.push(hijo);
                    }
                }
            }
        }
        return rolesEnJerarquia;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String correo = jwt.getClaimAsString("email");

        if (correo == null) return List.of();

        Optional<User> userOpt = userRepository.findUserWithRolesAndPermisosByCorreo(correo);
        if (userOpt.isEmpty()) return List.of();

        User user = userOpt.get();
        if (user.getEstado() != 1) return List.of();

        Rol rol = user.getRol();
        Set<Rol> roles = obtenerJerarquiaCompleta(rol);

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Rol r : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getNombreRol().toUpperCase()));
            for (Permiso permiso : r.getPermisos()) {
                authorities.add(new SimpleGrantedAuthority("PERM_" + permiso.getNombrePermiso().toUpperCase()));
            }
        }


        return authorities;
    }
}