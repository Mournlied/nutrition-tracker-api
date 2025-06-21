package com.mournlied.nutrition_tracker_api.domain.user.validaciones;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaveEsValidaTest {

    private ClaveEsValida validador;

    @BeforeEach
    void setUp() {
        validador = new ClaveEsValida();
    }

    // ========== TEST VALIDACION BASICA ==========
    @Test
    void ClaveValida_debeRetornarTrue() {
        assertTrue(validador.isValid("Aa11Bb22", null));
    }

    @Test
    void claveBajo8Caracteres_debeRetornarFalse() {
        assertFalse(validador.isValid("Aa11Bb2", null));
    }

    @Test
    void claveSobre12Caracteres_debeRetornarFalse() {
        assertFalse(validador.isValid("Aa11Bb22Cc33D", null));
    }

    @Test
    void insuficientesMayusculas_debeRetornarFalse() {
        assertFalse(validador.isValid("aabbcc11", null));
    }

    @Test
    void insuficientesMinusculas_debeRetornarFalse() {
        assertFalse(validador.isValid("AABBCC11", null));
    }

    @Test
    void insuficientesNumeros_debeRetornarFalse() {
        assertFalse(validador.isValid("AaBbCcDd", null));
    }

    @Test
    void claveNull_debeRetornarFalse() {
        assertFalse(validador.isValid(null, null));
    }

    // ========== TEST MENSAJES BASADOS EN CONTEXTO ==========

    @Test
    void debeRetornarMensajeErrorPorLongitud() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        boolean isValid = validador.isValid("A1b", context); // too short

        assertFalse(isValid);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("La clave debe contener entre 8 y 12 caracteres");
        verify(builder).addConstraintViolation();
    }

    @Test
    void debeRetornarMensajeErrorPorComplejidad() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        boolean isValid = validador.isValid("abcdefgh", context); // no uppercase or digits

        assertFalse(isValid);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(
                "La clave debe contener al menos 2 mayusculas, 2 minusculas y 2 numeros"
        );
        verify(builder).addConstraintViolation();
    }
}
