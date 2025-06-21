package com.mournlied.nutrition_tracker_api.domain.user.validaciones;

import com.mournlied.nutrition_tracker_api.domain.user.dto.RegistroUserDTO;
import com.mournlied.nutrition_tracker_api.infra.errores.ValidacionDeIntegridad;
import com.mournlied.nutrition_tracker_api.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDeUsers implements ConstraintValidator<ValidUser,RegistroUserDTO> {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(RegistroUserDTO registroUserDTO, ConstraintValidatorContext context){
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        var correo = userRepository.findUserByCorreo(registroUserDTO.correo());

        if (correo.isPresent()){
            throw new ValidacionDeIntegridad("Correo ya registrado");
        }
        return valid;
    }
}
