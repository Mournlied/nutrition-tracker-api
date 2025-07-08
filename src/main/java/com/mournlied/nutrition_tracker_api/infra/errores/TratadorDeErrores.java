package com.mournlied.nutrition_tracker_api.infra.errores;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;


@RestControllerAdvice
public class TratadorDeErrores {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<DatosError> tratarEntidadNoEncontrada404(EntityNotFoundException e, HttpServletRequest request){
        return error(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DatosError> tratarAccesoDenegado403(AccessDeniedException e, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DatosError> tratarNoAutorizado401(AuthenticationException e, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<DatosError> tratarExcepcionSeguridad401(SecurityException e, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    @ExceptionHandler(ObjetoRequeridoNoEncontrado.class)
    public ResponseEntity<DatosError> tratarObjetoRequeridoNoEncontrado400(ObjetoRequeridoNoEncontrado e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DatosError> tratarCuerpoInvalido400(HttpMessageNotReadableException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Falta el cuerpo de la solicitud o es inválido", request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<DatosError> tratarValidacionNegocio400(ValidationException e, HttpServletRequest request){
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(ValidacionDeIntegridad.class)
    public ResponseEntity<DatosError> tratarIntegridad409(ValidacionDeIntegridad e, HttpServletRequest request){
        return error(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DatosError> tratarViolacionIntegridadDeDatos409(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        String mensaje = "La operación viola una restricción de integridad.";

        Throwable root = getRootCause(e);

        if (root.getMessage() != null) {
            String msg = root.getMessage().toLowerCase();
            if (msg.contains("nombre_comida_unico")) {
                mensaje = "Ya existe una comida registrada con ese nombre.";
            } else if (msg.contains("fk_comida_user")) {
                mensaje = "El usuario no existe o ha sido eliminado.";
            }
        }

        return error(HttpStatus.CONFLICT, mensaje, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<DatosError> tratarErrorInterno500(IllegalStateException e, HttpServletRequest request){
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DatosErrorValidacion>> tratarArgumentosInvalidos400(MethodArgumentNotValidException e){
        List<DatosErrorValidacion> errores = e.getFieldErrors().stream()
                .map(DatosErrorValidacion::new)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    private ResponseEntity<DatosError> error(HttpStatus status, String mensaje, HttpServletRequest request) {
        DatosError datosError = new DatosError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(datosError);
    }

    private record DatosError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}

    private record DatosErrorValidacion(String dato, String error){
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause;
        while ((cause = throwable.getCause()) != null && cause != throwable) {
            throwable = cause;
        }
        return throwable;
    }
}
