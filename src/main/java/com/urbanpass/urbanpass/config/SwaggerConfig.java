package com.urbanpass.urbanpass.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UrbanPass API")
                        .version("1.0.0")
                        .description("Sistema de validación de tarjetas para transporte público. " +
                                "Inspirado en Transmetro, Guatemala 🇬🇹")
                        .contact(new Contact()
                                .name("Roberto")
                                .url("https://github.com/Roberto0199/urbanpass")))
                // Configurar autenticación JWT en Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa tu token JWT. Ejemplo: eyJhbGci...")));
    }
}