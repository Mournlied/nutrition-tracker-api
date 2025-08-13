package com.mournlied.nutrition_tracker_api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Hidden
public class HomeController {

    @GetMapping("/home")
    public Map<String, String> home() {
        return Map.of(
                "message", "Bienvenida o bienvenido a Nutrition Tracker API de TechParaTodxs",
                "version", "1.0.0",
                "documentation", "/swagger-ui.html",
                "api-docs", "/api-docs"
        );
    }
}