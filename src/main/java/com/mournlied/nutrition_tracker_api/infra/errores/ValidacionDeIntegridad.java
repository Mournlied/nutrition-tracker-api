package com.mournlied.nutrition_tracker_api.infra.errores;


public class ValidacionDeIntegridad extends RuntimeException {
    public ValidacionDeIntegridad(String s) {
        super(s);
    }
}