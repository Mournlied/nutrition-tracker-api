package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.user.HistorialPeso;
import com.mournlied.nutrition_tracker_api.domain.user.InformacionPersonal;
import com.mournlied.nutrition_tracker_api.domain.user.Rol;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.domain.user.dto.*;
import com.mournlied.nutrition_tracker_api.repository.HistorialPesoRepository;
import com.mournlied.nutrition_tracker_api.repository.InfoPersonalRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfoPersonalServiceTest {

    @Mock
    InfoPersonalRepository personalRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    HistorialPesoRepository historialPesoRepository;

    @InjectMocks
    InfoPersonalService infoPersonalService;

    @Mock
    Jwt jwt;

    private final User userDB = new User(
            1L,
            Collections.emptyList(),
            new InformacionPersonal(),
            new Rol(),
            LocalDate.of(2025,6,20),
            "test@example.com",
            1);

    private final HistorialPeso historialPeso = new HistorialPeso(
            1,
            new InformacionPersonal(),
            80,
            LocalDate.now()
    );

    private final List<HistorialPeso> historialPesos =
            new ArrayList<>(Arrays.asList(historialPeso, historialPeso, historialPeso, historialPeso));

    private final InformacionPersonal personalDB = new InformacionPersonal(
            1L,
            userDB,
            historialPesos,
            80,
            "test test",
            LocalDate.of(2000,1,1),
            180,
            "test");

    private final Pageable paginacion = PageRequest.of(0, 10);

    private final Page<HistorialPeso> paginaHistorialPeso = new PageImpl<>(
            List.of(historialPeso, historialPeso, historialPeso, historialPeso),
            paginacion,
            4);

    @BeforeEach
    void setup(){
        lenient().when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
    }

    @Test
    void testRegistrarInfoPersonal_requetsValida_debeRetornarNuevaInfoPersonal(){

        RegistroInfoPersonalDTO entradaDTO = new RegistroInfoPersonalDTO(
                81,
                "test test",
                LocalDate.of(2000,1,1),
                180,
                "test");

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));

        InformacionPersonalDTO salidaDTO = infoPersonalService.registrarInfoPersonal(jwt, entradaDTO);

        assertEquals(Collections.emptyList(), salidaDTO.historialPeso());
        assertEquals("test test", salidaDTO.nombre());
        assertEquals(LocalDate.of(2000,1,1), salidaDTO.nacimiento());

        verify(personalRepository).save(any(InformacionPersonal.class));
    }

    @Test
    void testHelper_userNoExiste_debeLanzarExcepcion(){

        RegistroInfoPersonalDTO entradaDTO = new RegistroInfoPersonalDTO(
                81,
                "test test",
                LocalDate.of(2000,1,1),
                180,
                "test");

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> infoPersonalService.registrarInfoPersonal(jwt, entradaDTO));
    }

    @Test
    void testActualizarInfoPersonalBase_requestValidaYTodosLosCampos_debeRetornarInfoPersonalActualizada(){

        ActualizarInfoPersonalBaseDTO entradaDTO = new ActualizarInfoPersonalBaseDTO(
                85,
                "nuevo test",
                LocalDate.of(2001,1,1),
                178,
                "nuevo test");

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(personalRepository.findByUserUserId(1L)).thenReturn(Optional.of(personalDB));

        InformacionPersonalDTO salidaDTO = infoPersonalService.actualizarInfoPersonalBase(jwt, entradaDTO);

        assertEquals(85, salidaDTO.pesoInicial());
        assertEquals("nuevo test", salidaDTO.nombre());
        assertEquals(LocalDate.of(2001,1,1), salidaDTO.nacimiento());
        assertEquals(4, salidaDTO.historialPeso().size());
    }

    @Test
    void testActualizarInfoPersonalBase_SoloNombreRestoNull_soloDebeActualizarNombre(){

        ActualizarInfoPersonalBaseDTO entradaDTO = new ActualizarInfoPersonalBaseDTO(
                null,
                "nuevo test",
                null,
                null,
                null);

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(personalRepository.findByUserUserId(1L)).thenReturn(Optional.of(personalDB));

        InformacionPersonalDTO salidaDTO = infoPersonalService.actualizarInfoPersonalBase(jwt, entradaDTO);

        assertEquals(80, salidaDTO.pesoInicial());
        assertEquals("nuevo test", salidaDTO.nombre());
        assertEquals(LocalDate.of(2000,1,1), salidaDTO.nacimiento());
        assertEquals(180, salidaDTO.altura());
        assertEquals("test", salidaDTO.objetivos());
    }

    @Test
    void testActualizarInfoPersonalBase_SoloAlturaRestoBlankONull_soloDebeActualizarAltura(){

        ActualizarInfoPersonalBaseDTO entradaDTO = new ActualizarInfoPersonalBaseDTO(
                null,
                "",
                null,
                178,
                "");

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(personalRepository.findByUserUserId(1L)).thenReturn(Optional.of(personalDB));

        InformacionPersonalDTO salidaDTO = infoPersonalService.actualizarInfoPersonalBase(jwt, entradaDTO);

        assertEquals("test test", salidaDTO.nombre());
        assertEquals(178, salidaDTO.altura());
        assertEquals("test", salidaDTO.objetivos());
    }

    @Test
    void testHelper_infoPersonalNoExiste_debeLanzarExcepcion(){

        ActualizarInfoPersonalBaseDTO entradaDTO = new ActualizarInfoPersonalBaseDTO(
                null,
                "",
                null,
                178,
                "");

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(personalRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> infoPersonalService.actualizarInfoPersonalBase(jwt, entradaDTO));
    }

    @Test
    void testActualizarHistorialPeso_requestValida_debeRetornarNuevaPaginaHistorialPeso(){

        RegistroHistorialPesoDTO entradaDTO = new RegistroHistorialPesoDTO(79);

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(personalRepository.findByUserUserId(1L)).thenReturn(Optional.of(personalDB));

        Page<ObtenerHistorialPesoDTO> paginaSalida = infoPersonalService.actualizarHistorialPeso(
                jwt, paginacion ,entradaDTO);

        assertEquals(5, paginaSalida.getContent().size());
        assertEquals(79, paginaSalida.getContent().get(4).pesoActual());
        assertEquals(LocalDate.now(), paginaSalida.getContent().get(4).fechaActual());
    }

    @Test
    void testObtenerHistorialPeso_requestValida_debeRetornarPaginaHistorialPeso(){

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(historialPesoRepository.findByPersonalInfo_InfoPersonalId(1L, paginacion))
                .thenReturn(paginaHistorialPeso);

        Page<ObtenerHistorialPesoDTO> paginaSalida = infoPersonalService.obtenerHistorialPeso(jwt, paginacion);

        assertEquals(4, paginaSalida.getContent().size());
        assertEquals(80, paginaSalida.getContent().get(0).pesoActual());
        assertEquals(LocalDate.now(), paginaSalida.getContent().get(0).fechaActual());
    }
}