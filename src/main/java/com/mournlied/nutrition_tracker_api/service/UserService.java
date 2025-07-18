package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.user.InformacionPersonal;
import com.mournlied.nutrition_tracker_api.domain.user.Rol;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ActualizarUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserAdminRequestDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.ObtenerUserDTO;
import com.mournlied.nutrition_tracker_api.domain.user.dto.UserCreadoDTO;
import com.mournlied.nutrition_tracker_api.infra.errores.ObjetoRequeridoNoEncontrado;
import com.mournlied.nutrition_tracker_api.infra.errores.ValidacionDeIntegridad;
import com.mournlied.nutrition_tracker_api.repository.RolRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;

    public UserService (UserRepository userRepository, RolRepository rolRepository){

        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public UserCreadoDTO crearUser(Authentication authentication) {

        log.info("Request ID {}", MDC.get("requestId"));
        User user = validaYCreaUserDesdeAutenticacion(authentication);
        log.debug("Consultando DB por rol id: 1");
        Rol rol = rolRepository.findById(1).orElseThrow(()-> new IllegalStateException("Rol 1L no encontrado."));
        user.setRol(rol);

        log.info("Creando user");
        userRepository.save(user);
        user.setInfoPersonal(new InformacionPersonal(user));

        return new UserCreadoDTO(user);
    }

    public ObtenerUserDTO obtenerUserPorId (Long id, Authentication authentication) {

        log.info("Request ID {}", MDC.get("requestId"));
        log.info("Obteniendo user id: {}", id);
        return new ObtenerUserDTO(validarIdPerteneceAUserAutenticado(id, authentication));
    }

    @Transactional
    public void eliminarUser(Long id, Authentication authentication) {

        log.info("Request ID {}", MDC.get("requestId"));
        log.info("Eliminando user id: {}", id);
        userRepository.delete(validarIdPerteneceAUserAutenticado(id, authentication));
    }

    public Page<ObtenerUserAdminRequestDTO> obtenerAllUsers (Pageable paginacion){

        log.info("Request ID {}", MDC.get("requestId"));
        log.info("Obteniendo lista de users");
        return userRepository.findAll(paginacion).map(ObtenerUserAdminRequestDTO::new);
    }

    @Transactional
    public ObtenerUserAdminRequestDTO actualizarUser(Long id, ActualizarUserDTO modDTO) {

        log.info("Request ID {}", MDC.get("requestId"));
        User userInDB = obtenerUserDesdeUserId(id);

        log.info("Actualizando user id: {}", id);
        if (!(modDTO.rolId()==null)) {
            log.debug("Consultando DB por rol id: {}", modDTO.rolId());
            var rolNuevo = rolRepository.findRolByRolId(modDTO.rolId());
            if (rolNuevo.isEmpty()){throw new ObjetoRequeridoNoEncontrado("Rol no existe.");}
            userInDB.setRol(rolNuevo.get());
        }
        if (!(modDTO.estado()==null)){userInDB.setEstado(modDTO.estado());}

        return new ObtenerUserAdminRequestDTO(userInDB);
    }

    @Transactional
    public void eliminarUserAdmins(Long id) {

        log.info("Request ID {}", MDC.get("requestId"));
        log.info("Eliminando user id: {}", id);
        userRepository.delete(obtenerUserDesdeUserId(id));
    }

    private User validaYCreaUserDesdeAutenticacion(Authentication authentication){

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Boolean verified = jwt.getClaimAsBoolean("email_verified");
        if (verified == null || !verified) {throw new SecurityException("Primero debe verificar su correo.");}

        String correoToken = jwt.getClaimAsString("email");

        log.debug("Consultando DB por user: {}", correoToken);
        var correo = userRepository.findUserByCorreo(correoToken);
        if (correo.isPresent()){throw new ValidacionDeIntegridad("Correo ya registrado.");}

        return new User(correoToken);
    }

    private User obtenerUserDesdeUserId (Long id){

        log.debug("Consultando DB por user id: {}", id);
        var user = userRepository.findUserByUserId(id);

        if (user.isEmpty()) {throw new ObjetoRequeridoNoEncontrado("User no existe.");}

        return user.get();
    }

    private User validarIdPerteneceAUserAutenticado(Long id, Authentication authentication){

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String correoLoggeado = jwt.getClaimAsString("email");

        log.debug("Consultando DB por user: {}", correoLoggeado);
        var userLoggeado = userRepository.findUserByCorreo(correoLoggeado);

        if (userLoggeado.isEmpty()) {throw new ObjetoRequeridoNoEncontrado("User no registrada/o.");}

        if (!userLoggeado.get().getUserId().equals(id)) {
            throw new AccessDeniedException("Id no corresponde a la cuenta ingresada actualmente.");
        }

        return userLoggeado.get();
    }
}
