package com.mournlied.nutrition_tracker_api.infra.errores;

import org.springframework.security.core.AuthenticationException;

public class CorreoIngresadoYJwtDiferentesException extends AuthenticationException {
    public CorreoIngresadoYJwtDiferentesException(String s) {
        super(s);
    }
}
