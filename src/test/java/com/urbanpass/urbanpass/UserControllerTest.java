package com.urbanpass.urbanpass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanpass.urbanpass.controller.UserController;
import com.urbanpass.urbanpass.dto.CardResponse;
import com.urbanpass.urbanpass.dto.CreateUserRequest;
import com.urbanpass.urbanpass.dto.UserResponse;
import com.urbanpass.urbanpass.enums.CardStatus;
import com.urbanpass.urbanpass.exception.BusinessException;
import com.urbanpass.urbanpass.exception.GlobalExceptionHandler;
import com.urbanpass.urbanpass.exception.ResourceNotFoundException;
import com.urbanpass.urbanpass.security.JwtAuthFilter;
import com.urbanpass.urbanpass.security.JwtService;
import com.urbanpass.urbanpass.security.SecurityConfig;
import com.urbanpass.urbanpass.service.CardService;
import com.urbanpass.urbanpass.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserResponse userResponse;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .name("Carlos Pérez")
                .email("carlos@gmail.com")
                .phone("55551234")
                .createdAt(LocalDateTime.now())
                .build();

        cardResponse = CardResponse.builder()
                .id(1L)
                .cardNumber("5200123456789012")
                .balance(new BigDecimal("0.00"))
                .status(CardStatus.ACTIVE)
                .userId(1L)
                .userName("Carlos Pérez")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUser_shouldReturn200_whenUserExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Carlos Pérez"))
                .andExpect(jsonPath("$.email").value("carlos@gmail.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUser_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("Usuario", 99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void getUser_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturn200_whenAdmin() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Carlos Pérez"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn201_whenAdmin() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Ana López");
        request.setEmail("ana@gmail.com");
        request.setPhone("55559999");

        when(userService.createUser(any())).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn400_whenPhoneIsInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Ana López");
        request.setEmail("ana@gmail.com");
        request.setPhone("abc");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_shouldReturn403_whenNotAdmin() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Ana López");
        request.setEmail("ana@gmail.com");
        request.setPhone("55559999");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void issueCard_shouldReturn201_whenAdmin() throws Exception {
        when(cardService.issueCard(1L)).thenReturn(cardResponse);

        mockMvc.perform(post("/api/users/1/cards").with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardNumber").value("5200123456789012"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void issueCard_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/users/1/cards").with(csrf()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).issueCard(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void issueCard_shouldReturn404_whenUserNotFound() throws Exception {
        when(cardService.issueCard(99L))
                .thenThrow(new ResourceNotFoundException("Usuario", 99L));

        mockMvc.perform(post("/api/users/99/cards").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_shouldReturn200_withCardList() throws Exception {
        when(cardService.getCardsByUser(1L)).thenReturn(List.of(cardResponse));

        mockMvc.perform(get("/api/users/1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardNumber").value("5200123456789012"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_shouldReturn200_withEmptyList() throws Exception {
        when(cardService.getCardsByUser(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}