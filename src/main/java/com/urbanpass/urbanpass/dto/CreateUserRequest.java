package com.urbanpass.urbanpass.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "Teléfono debe tener 8 dígitos")
    private String phone;
}