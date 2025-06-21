package com.mournlied.nutrition_tracker_api.domain.user.validaciones;

import com.mournlied.nutrition_tracker_api.domain.user.User;
import com.mournlied.nutrition_tracker_api.domain.user.dto.RegistroUserDTO;
import com.mournlied.nutrition_tracker_api.infra.errores.ValidacionDeIntegridad;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorDeUsersTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private ValidadorDeUsers validador;

    private RegistroUserDTO registroUserDTO;

    @BeforeEach
    void setup() {
        registroUserDTO = mock(RegistroUserDTO.class);
    }

    @Test
    void cuandoElCorreoNoExisteEnDB_debeRetornarTrue() {

        when(registroUserDTO.correo()).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.empty());

        boolean resultado = validador.isValid(registroUserDTO, context);

        assertTrue(resultado);
    }

    @Test
    void cuandoElCorreoYaExisteEnDB_debeTirarExcepcionValidacionDeIntegridad(){

        when(registroUserDTO.correo()).thenReturn("test@example.com");
        when(userRepository.findUserByCorreo("test@example.com")).thenReturn(Optional.of(mock(User.class)));

        assertThrows(ValidacionDeIntegridad.class,()->validador.isValid(registroUserDTO,context));
    }
}