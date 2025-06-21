package com.mournlied.nutrition_tracker_api.infra.errores;

public class ObjetoRequeridoNoEncontrado extends RuntimeException {
    public ObjetoRequeridoNoEncontrado(String message) {

        super(message);
    }
}
