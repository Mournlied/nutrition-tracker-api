package com.mournlied.nutrition_tracker_api.domain.user.validaciones;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidadorDeUsers.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUser {
    String message() default "User invalido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
