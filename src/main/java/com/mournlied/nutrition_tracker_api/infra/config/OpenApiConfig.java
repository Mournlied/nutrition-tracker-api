package com.mournlied.nutrition_tracker_api.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutritionTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nutrition Tracker API")
                        .description("API para hacer seguimiento de comidas y peso")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Diego Leon")
                                .email("diego.leon.bnss@gmail.com")
                                .url("https://github.com/Mournlied/nutrition-tracker-api")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Dev server"),
                        new Server().url("https://techparatodxs.com").description("Prod server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresar JWT token obtenido desde Auth0")));
    }
}