package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.comida.Comida;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ActualizarComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ObtenerComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.repository.ComidaRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class ComidaService {

    private final ComidaRepository comidaRepository;
    private final UserRepository userRepository;

    public ComidaService(ComidaRepository comidaRepository, UserRepository userRepository){
        this.comidaRepository = comidaRepository;
        this.userRepository = userRepository;
    }

    public Page<ObtenerComidaDTO> obtenerListaComidas(Jwt jwt, Pageable paginacion,
                                                      LocalDate startDate, LocalDate endDate) {

        log.debug("Request ID {}", MDC.get("requestId"));

        Long userId = obtenerUserDesdeJwt(jwt).getUserId();

        RangoFechas fechas = ajustaFechasParaBusqueda(startDate,endDate);

        log.info("Obteniendo lista de comidas");
        return comidaRepository.findByUserUserIdAndFechaCreacionComidaBetween(
                userId, fechas.startDate, fechas.endDate, paginacion).map(ObtenerComidaDTO::new);
    }

    public ObtenerComidaDTO registrarNuevaComida(Jwt jwt, @Valid RegistroComidaDTO registroComidaDTO) {

        log.debug("Request ID {}", MDC.get("requestId"));

        User user = obtenerUserDesdeJwt(jwt);

        log.info("Creando nueva comida: {}", registroComidaDTO.nombreComida());
        Comida nuevaComida = new Comida(registroComidaDTO, user);
        comidaRepository.save(nuevaComida);

        return new ObtenerComidaDTO(nuevaComida);
    }

    public Page<ObtenerComidaDTO> obtenerListaComidasFavoritas(Jwt jwt, Pageable paginacion) {

        log.debug("Request ID {}", MDC.get("requestId"));

        Long userId = obtenerUserDesdeJwt(jwt).getUserId();

        log.info("Obteniendo lista de comidas favoritas");
        return comidaRepository.findByUserUserIdAndEsFavoritaTrue(userId,paginacion).map(ObtenerComidaDTO::new);
    }

    @Transactional
    public ObtenerComidaDTO actualizarComida(ActualizarComidaDTO actualizarComidaDTO) {

        log.debug("Request ID {}", MDC.get("requestId"));

        String nombreComida = actualizarComidaDTO.nombreComidaOriginal();

        log.info("Actualizando comida: {}", nombreComida);
        Comida comida = obtenerComidaConNombreComida(nombreComida);

        patchComidaDesdeDto(comida, actualizarComidaDTO);

        return new ObtenerComidaDTO(comida);
    }

    public void eliminarComida(@NotBlank String nombreComida) {

        log.debug("Request ID {}", MDC.get("requestId"));
        log.info("Eliminando comida: {}", nombreComida);
        comidaRepository.delete(obtenerComidaConNombreComida(nombreComida));
    }

    private User obtenerUserDesdeJwt(Jwt jwt){

        String correoToken = jwt.getClaimAsString("email");

        log.debug("Consultando DB por correo: {}", correoToken);
        var user = userRepository.findUserByCorreo(correoToken);

        if (user.isEmpty()){throw new EntityNotFoundException("User no existe.");}

        return user.get();
    }

    private Comida obtenerComidaConNombreComida(String nombreComida){

        log.debug("Consultando DB por comida: {}", nombreComida);
        var comidaDB = comidaRepository.findByNombreComida(nombreComida);

        if (comidaDB.isEmpty()){throw new EntityNotFoundException("Comida no existe.");}

        return comidaDB.get();
    }

    private void patchComidaDesdeDto(Comida comida, ActualizarComidaDTO dto){
        if (dto.nombreComidaNuevo() != null && !dto.nombreComidaNuevo().isBlank()) {
            comida.setNombreComida(dto.nombreComidaNuevo());
        }

        if (dto.cantidadEnGramos() != null) {
            comida.setCantidadEnGramos(dto.cantidadEnGramos());
        }

        if (dto.descripcion() != null && !dto.descripcion().isBlank()) {
            comida.setDescripcion(dto.descripcion());
        }

        if (dto.tipoComida() != null && !dto.tipoComida().isBlank()) {
            comida.setTipoComida(dto.tipoComida());
        }

        if (dto.informacionNutricional() != null) {
            comida.setInformacionNutricional(dto.informacionNutricional());
        }

        if (dto.esFavorita() != null) {
            comida.setEsFavorita(dto.esFavorita());
        }
    }

    private RangoFechas ajustaFechasParaBusqueda(LocalDate startDate, LocalDate endDate){

        if (endDate == null){
            endDate = LocalDate.now();
        }

        if (startDate == null){
            startDate = endDate.minusDays(6);
        }

        return new RangoFechas(startDate, endDate);
    }

    private record RangoFechas(LocalDate startDate, LocalDate endDate) {}
}
