package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.user.HistorialPeso;
import com.mournlied.nutrition_tracker_api.domain.user.InformacionPersonal;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.domain.user.dto.*;
import com.mournlied.nutrition_tracker_api.repository.HistorialPesoRepository;
import com.mournlied.nutrition_tracker_api.repository.InfoPersonalRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InfoPersonalService {

    private final InfoPersonalRepository personalRepository;
    private final UserRepository userRepository;
    private final HistorialPesoRepository historialPesoRepository;

    public InfoPersonalService (InfoPersonalRepository personalRepository,
                                UserRepository userRepository,
                                HistorialPesoRepository historialPesoRepository){
        this.personalRepository = personalRepository;
        this.userRepository = userRepository;
        this.historialPesoRepository = historialPesoRepository;
    }


    public InformacionPersonalDTO registrarInfoPersonal(Jwt jwt, @Valid RegistroInfoPersonalDTO registroDTO) {

        log.info("Request ID {}", MDC.get("requestId"));
        User user = obtenerUserDesdeJwt(jwt);

        InformacionPersonal informacionPersonal = new InformacionPersonal(registroDTO, user);

        log.info("Creando nueva informacion personal");
        personalRepository.save(informacionPersonal);

        return new InformacionPersonalDTO(informacionPersonal);
    }

    @Transactional
    public InformacionPersonalDTO actualizarInfoPersonalBase(Jwt jwt, @Valid ActualizarInfoPersonalBaseDTO actualizarDTO) {

        log.info("Request ID {}", MDC.get("requestId"));
        InformacionPersonal infoPersonal = obtenerInfoPersonalConUserId(obtenerUserDesdeJwt(jwt).getUserId());

        log.info("Actualizando informacion personal");
        patchInfoPersonalBaseDesdeDTO(infoPersonal, actualizarDTO);

        return new InformacionPersonalDTO(infoPersonal);
    }

    @Transactional
    public Page<ObtenerHistorialPesoDTO> actualizarHistorialPeso(
            Jwt jwt, Pageable paginacion, @Valid RegistroHistorialPesoDTO registroHistorialPesoDTO) {

        log.info("Request ID {}", MDC.get("requestId"));
        InformacionPersonal infoPersonal = obtenerInfoPersonalConUserId(obtenerUserDesdeJwt(jwt).getUserId());

        log.info("Actualizando historial de peso");
        infoPersonal.getHistorialPeso().add(new HistorialPeso(registroHistorialPesoDTO));

        List<ObtenerHistorialPesoDTO> listaDTO = infoPersonal.getHistorialPeso().stream()
                .map(ObtenerHistorialPesoDTO::new)
                .toList();

        return new PageImpl<>(
                listaDTO,
                paginacion,
                listaDTO.size());
    }

    public Page<ObtenerHistorialPesoDTO> obtenerHistorialPeso(Jwt jwt, Pageable paginacion) {

        log.info("Request ID {}", MDC.get("requestId"));
        var historialPeso = historialPesoRepository.findByPersonalInfo_InfoPersonalId(
                obtenerUserDesdeJwt(jwt).getUserId(),paginacion);
        log.info("Obteniendo historial de peso");

        return historialPeso.map(ObtenerHistorialPesoDTO::new);
    }

    private User obtenerUserDesdeJwt(Jwt jwt){

        String correoToken = jwt.getClaimAsString("email");

        log.debug("Consultando DB por user: {}", correoToken);
        var user = userRepository.findUserByCorreo(correoToken);

        if (user.isEmpty()){throw new EntityNotFoundException("User no existe.");}

        return user.get();
    }

    private InformacionPersonal obtenerInfoPersonalConUserId(Long userId){

        log.debug("Consultando DB por info personal de userId: {}", userId);
        var infoPersonalOpt = personalRepository.findByUserUserId(userId);

        if (infoPersonalOpt.isEmpty()){throw new EntityNotFoundException("user no existe.");}

        return infoPersonalOpt.get();
    }

    private void patchInfoPersonalBaseDesdeDTO(InformacionPersonal infoPersonal, ActualizarInfoPersonalBaseDTO dto){

        if (dto.pesoInicial() != null){
            infoPersonal.setPesoInicial(dto.pesoInicial());
        }

        if (dto.nombre() != null && !dto.nombre().isBlank()){
            infoPersonal.setNombre(dto.nombre());
        }

        if (dto.nacimiento() != null){
            infoPersonal.setNacimiento(dto.nacimiento());
        }

        if (dto.altura() != null){
            infoPersonal.setAltura(dto.altura());
        }

        if (dto.objetivos() != null && !dto.objetivos().isBlank()){
            infoPersonal.setObjetivos(dto.objetivos());
        }
    }
}
