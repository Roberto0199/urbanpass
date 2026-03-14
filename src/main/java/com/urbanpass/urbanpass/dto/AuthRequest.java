package com.urbanpass.urbanpass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Datos necesarios para autenticación")
public class AuthRequest {

    @Email
    @NotBlank
    @Schema(description = "Correo del usuario", example = "usuario@email.com")
    private String email;

    @NotBlank
    @Schema(description = "Contraseña del usuario", example = "password123")
    private String password;
}