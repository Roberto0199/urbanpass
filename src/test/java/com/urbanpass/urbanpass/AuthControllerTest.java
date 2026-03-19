package com.urbanpass.urbanpass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanpass.urbanpass.controller.AuthController;
import com.urbanpass.urbanpass.dto.AuthResponse;
import com.urbanpass.urbanpass.dto.LoginRequest;
import com.urbanpass.urbanpass.dto.RegisterRequest;
import com.urbanpass.urbanpass.exception.BusinessException;
import com.urbanpass.urbanpass.exception.GlobalExceptionHandler;
import com.urbanpass.urbanpass.security.JwtAuthFilter;
import com.urbanpass.urbanpass.security.JwtService;
import com.urbanpass.urbanpass.security.SecurityConfig;
import com.urbanpass.urbanpass.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = new AuthResponse("jwt-token-mock", "carlos@gmail.com", "Carlos Pérez");
    }

    @Test
    void register_shouldReturn200_whenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Carlos Pérez");
        request.setEmail("carlos@gmail.com");
        request.setPassword("Password123!");
        request.setPhone("55551234");

        when(authService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-mock"))
                .andExpect(jsonPath("$.email").value("carlos@gmail.com"));
    }

    @Test
    void register_shouldReturn400_whenNameIsBlank() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("carlos@gmail.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Carlos");
        request.setEmail("no-es-un-email");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_shouldReturn422_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Carlos Pérez");
        request.setEmail("duplicado@gmail.com");
        request.setPassword("Password123!");

        when(authService.register(any()))
                .thenThrow(new BusinessException("El email ya está registrado."));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("El email ya está registrado."));
    }

    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("carlos@gmail.com");
        request.setPassword("Password123!");

        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("carlos@gmail.com"));
    }

    @Test
    void login_shouldReturn401_whenPasswordIsWrong() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("carlos@gmail.com");
        request.setPassword("ContraseñaMal");

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Correo o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void login_shouldReturn401_whenUserDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("fantasma@gmail.com");
        request.setPassword("cualquier");

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Correo o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}