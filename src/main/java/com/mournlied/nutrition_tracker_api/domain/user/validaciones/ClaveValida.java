package com.mournlied.nutrition_tracker_api.domain.user.validaciones;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ClaveEsValida.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClaveValida {
    String message() default "Clave invalida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
