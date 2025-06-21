package com.mournlied.nutrition_tracker_api.domain.user.validaciones;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ClaveEsValida implements ConstraintValidator<ClaveValida, String> {
    @Override
    public boolean isValid(String clave, ConstraintValidatorContext context) {
        if (clave == null) return false;

        if (clave.length() < 8 || clave.length() > 12) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("La clave debe contener entre 8 y 12 caracteres")
                        .addConstraintViolation();
            }
            return false;
        }

        long mayus = clave.chars().filter(Character::isUpperCase).count();
        long minus = clave.chars().filter(Character::isLowerCase).count();
        long numeros = clave.chars().filter(Character::isDigit).count();

        if (mayus < 2 || minus < 2 || numeros < 2) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "La clave debe contener al menos 2 mayusculas, 2 minusculas y 2 numeros"
                ).addConstraintViolation();
            }
            return false;
        }

        return true;
    }
}
