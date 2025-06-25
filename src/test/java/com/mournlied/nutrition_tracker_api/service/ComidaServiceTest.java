package com.mournlied.nutrition_tracker_api.service;

import com.mournlied.nutrition_tracker_api.domain.comida.Comida;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ActualizarComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.ObtenerComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.user.*;
import com.mournlied.nutrition_tracker_api.repository.ComidaRepository;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComidaServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComidaRepository comidaRepository;

    @InjectMocks
    private ComidaService comidaService;

    @Mock
    private Jwt jwt;

    private final User userDB = new User(
            1L,
            Collections.emptyList(),
            new InformacionPersonal(),
            new Rol(),
            LocalDate.of(2025,6,20),
            "test@example.com",
            1);

    private final Comida comidaDB = new Comida(
            1L,
            userDB,
            LocalDate.of(2025,6,23),
            "test",
            500,
            "test",
            "test",
            Map.of("proteins",23, "carbs", 46, "total fats", 12),
            true
    );

    private final Pageable paginacion = PageRequest.of(0, 10);

    private final Page<Comida> paginaComidas = new PageImpl<>(
            List.of(comidaDB,comidaDB),
            paginacion,
            2);

    @BeforeEach
    void setup(){
        lenient().when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
    }

    @Test
    void testObtenerListaComida_userExisteYNoContieneFechas_debeRetornarPaginaComidas(){

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(comidaRepository.findByUserUserIdAndFechaCreacionComidaBetween(
                any(Long.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(paginaComidas);

        Page<ObtenerComidaDTO> paginaSalida =
                comidaService.obtenerListaComidas(jwt, paginacion, null, null);

        assertEquals(2, paginaSalida.getContent().size());
        assertEquals("test", paginaSalida.getContent().get(0).nombreComida());
        assertEquals(10, paginaSalida.getPageable().getPageSize());

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(comidaRepository).findByUserUserIdAndFechaCreacionComidaBetween(
                any(Long.class),startCaptor.capture(),endCaptor.capture(), any(Pageable.class));

        assertEquals(LocalDate.now(), endCaptor.getValue());
        assertEquals(LocalDate.now().minusDays(6), startCaptor.getValue());
    }

    @Test
    void testObtenerListaComida_userExisteYContieneSoloStartDate_debeRetornarPaginaComidas(){

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(comidaRepository.findByUserUserIdAndFechaCreacionComidaBetween(
                any(Long.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(paginaComidas);

        Page<ObtenerComidaDTO> paginaSalida = comidaService.obtenerListaComidas(
                jwt, paginacion, LocalDate.now().minusDays(10), null);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(comidaRepository).findByUserUserIdAndFechaCreacionComidaBetween(
                any(Long.class),startCaptor.capture(),endCaptor.capture(), any(Pageable.class));

        assertEquals(LocalDate.now(), endCaptor.getValue());
        assertEquals(LocalDate.now().minusDays(10), startCaptor.getValue());
    }

    @Test
    void testObtenerListaComida_userExisteYContieneSoloEndDate_debeRetornarPaginaComidas(){

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(comidaRepository.findByUserUserIdAndFechaCreacionComidaBetween(
                any(Long.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(paginaComidas);

        Page<ObtenerComidaDTO> paginaSalida = comidaService.obtenerListaComidas(
                jwt, paginacion, null, LocalDate.now().minusDays(7));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(comidaRepository).findByUserUserIdAndFechaCreacionComidaBetween(
                any(Long.class),startCaptor.capture(),endCaptor.capture(), any(Pageable.class));

        assertEquals(LocalDate.now().minusDays(7), endCaptor.getValue());
        assertEquals(LocalDate.now().minusDays(13), startCaptor.getValue());
    }

    @Test
    void testHelper_userNoExiste_debeLanzarExcepcion(){

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> comidaService.obtenerListaComidas(jwt,paginacion,null,null));
    }

    @Test
    void testRegistrarNuevaComida_requestValida_debeRetornarNuevaComida(){

        RegistroComidaDTO entradaDTO = new RegistroComidaDTO(
                "test",
                500,
                "test",
                "test",
                Map.of("proteins",23, "carbs", 46, "total fats", 12),
                true);

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));

        ObtenerComidaDTO salidaDTO = comidaService.registrarNuevaComida(jwt, entradaDTO);

        assertEquals("test", salidaDTO.nombreComida());
        assertEquals(500, salidaDTO.cantidadEnGramos());
        assertEquals(Map.of("proteins",23, "carbs", 46, "total fats", 12),
                salidaDTO.informacionNutricional());

        ArgumentCaptor<Comida> comidaCaptor = ArgumentCaptor.forClass(Comida.class);

        verify(comidaRepository).save(comidaCaptor.capture());

        assertEquals(userDB, comidaCaptor.getValue().getUser());
        assertEquals(LocalDate.now(), comidaCaptor.getValue().getFechaCreacionComida());
        assertEquals(true, comidaCaptor.getValue().getEsFavorita());
    }

    @Test
    void testObtenerListaComidasFavoritas_userExiste_debeRetornaPaginaComidas(){

        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(userDB));
        when(comidaRepository.findByUserUserIdAndEsFavoritaTrue(
                any(Long.class), any(Pageable.class)))
                .thenReturn(paginaComidas);

        Page<ObtenerComidaDTO> paginaSalida =
                comidaService.obtenerListaComidasFavoritas(jwt, paginacion);

        assertEquals(2, paginaSalida.getContent().size());
        assertEquals("test", paginaSalida.getContent().get(0).nombreComida());
        assertEquals(10, paginaSalida.getPageable().getPageSize());
    }

    @Test
    void testActualizarComida_OriginalExisteYTodosLosCampos_debeRetornarComidaActualizada(){

        ActualizarComidaDTO entradaDTO = new ActualizarComidaDTO(
                "test",
                "nuevo test",
                1000,
                "nueva test",
                "nuevo test",
                Map.of("proteins",23, "carbs", 46, "total fats", 12, "sodium", 124),
                false
        );

        when(comidaRepository.findByNombrecomida("test")).thenReturn(Optional.of(comidaDB));

        ObtenerComidaDTO salidaDTO = comidaService.actualizarComida(entradaDTO);

        assertEquals("nuevo test", salidaDTO.nombreComida());
        assertEquals(Map.of("proteins",23, "carbs", 46, "total fats", 12, "sodium", 124),
                salidaDTO.informacionNutricional());
        assertEquals(1000, salidaDTO.cantidadEnGramos());
    }

    @Test
    void testActualizarComida_OriginalExisteYSoloNombreNuevoRestoNull_debeSoloActualizarNombre(){

        ActualizarComidaDTO entradaDTO = new ActualizarComidaDTO(
                "test",
                "nuevo test",
                null,
                null,
                null,
                null,
                null
        );

        when(comidaRepository.findByNombrecomida("test")).thenReturn(Optional.of(comidaDB));

        ObtenerComidaDTO salidaDTO = comidaService.actualizarComida(entradaDTO);

        assertEquals("nuevo test", salidaDTO.nombreComida());
        assertEquals("test", salidaDTO.tipoComida());
        assertEquals("test", salidaDTO.descripcion());
        assertEquals(Map.of("proteins",23, "carbs", 46, "total fats", 12),
                salidaDTO.informacionNutricional());
        assertEquals(500, salidaDTO.cantidadEnGramos());
    }

    @Test
    void testActualizarComida_OriginalExisteYSoloCantidadRestoEnBlancoSinoNull_debeSoloActualizarCantidad(){

        ActualizarComidaDTO entradaDTO = new ActualizarComidaDTO(
                "test",
                "",
                1000,
                "",
                "",
                null,
                null
        );

        when(comidaRepository.findByNombrecomida("test")).thenReturn(Optional.of(comidaDB));

        ObtenerComidaDTO salidaDTO = comidaService.actualizarComida(entradaDTO);

        assertEquals("test", salidaDTO.nombreComida());
        assertEquals("test", salidaDTO.tipoComida());
        assertEquals("test", salidaDTO.descripcion());
        assertEquals(Map.of("proteins",23, "carbs", 46, "total fats", 12),
                salidaDTO.informacionNutricional());
        assertEquals(1000, salidaDTO.cantidadEnGramos());
    }

    @Test
    void testEliminarComida_comidaExiste_debeEliminarComida(){

        when(comidaRepository.findByNombrecomida("test")).thenReturn(Optional.of(comidaDB));

        comidaService.eliminarComida("test");

        verify(comidaRepository).delete(comidaDB);
    }

    @Test
    void testHelper_comidaNoExiste_debeLanzarExcepcion(){

        when(comidaRepository.findByNombrecomida("test")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> comidaService.eliminarComida("test"));
    }
}