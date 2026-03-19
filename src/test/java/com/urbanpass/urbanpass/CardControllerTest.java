package com.urbanpass.urbanpass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanpass.urbanpass.controller.CardController;
import com.urbanpass.urbanpass.dto.*;
import com.urbanpass.urbanpass.enums.CardStatus;
import com.urbanpass.urbanpass.enums.TransactionStatus;
import com.urbanpass.urbanpass.exception.BusinessException;
import com.urbanpass.urbanpass.exception.GlobalExceptionHandler;
import com.urbanpass.urbanpass.exception.ResourceNotFoundException;
import com.urbanpass.urbanpass.security.JwtAuthFilter;
import com.urbanpass.urbanpass.security.JwtService;
import com.urbanpass.urbanpass.security.SecurityConfig;
import com.urbanpass.urbanpass.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(CardController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        cardResponse = CardResponse.builder()
                .id(1L)
                .cardNumber("5200123456789012")
                .balance(new BigDecimal("80.00"))
                .status(CardStatus.ACTIVE)
                .userId(1L)
                .userName("Carlos Pérez")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void recharge_shouldReturn200_whenValidRequest() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("30.00"));

        when(cardService.recharge(eq(1L), any())).thenReturn(cardResponse);

        mockMvc.perform(post("/api/cards/1/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(80.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recharge_shouldReturn400_whenAmountBelowMinimum() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("5.00"));

        mockMvc.perform(post("/api/cards/1/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recharge_shouldReturn400_whenAmountIsNull() throws Exception {
        mockMvc.perform(post("/api/cards/1/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recharge_shouldReturn404_whenCardNotFound() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("50.00"));

        when(cardService.recharge(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Tarjeta", 99L));

        mockMvc.perform(post("/api/cards/99/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recharge_shouldReturn422_whenCardIsBlocked() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("50.00"));

        when(cardService.recharge(eq(1L), any()))
                .thenThrow(new BusinessException("La tarjeta no está activa."));

        mockMvc.perform(post("/api/cards/1/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("La tarjeta no está activa."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void validateCard_shouldReturn200_whenApproved() throws Exception {
        ValidateCardRequest request = new ValidateCardRequest();
        request.setStationId(1L);

        ValidationResponse response = ValidationResponse.builder()
                .approved(true)
                .message("¡Bienvenido! Buen viaje.")
                .balanceAfter(new BigDecimal("45.00"))
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        when(cardService.validateCard(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/cards/1/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.message").value("¡Bienvenido! Buen viaje."))
                .andExpect(jsonPath("$.balanceAfter").value(45.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void validateCard_shouldReturn400_whenStationIdIsNull() throws Exception {
        mockMvc.perform(post("/api/cards/1/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void validateCard_shouldReturn422_whenInsufficientBalance() throws Exception {
        ValidateCardRequest request = new ValidateCardRequest();
        request.setStationId(1L);

        when(cardService.validateCard(eq(1L), any()))
                .thenThrow(new BusinessException("Saldo insuficiente. Saldo actual: Q1.00 | Requerido: Q5.00"));

        mockMvc.perform(post("/api/cards/1/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHistory_shouldReturn200_withPaginatedResults() throws Exception {
        TransactionResponse tx = TransactionResponse.builder()
                .id(1L)
                .cardId(1L)
                .cardNumber("5200123456789012")
                .amount(new BigDecimal("5.00"))
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        var page = new PageImpl<>(List.of(tx), PageRequest.of(0, 10), 1);
        when(cardService.getCardHistory(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/cards/1/history")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(5.00))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHistory_shouldReturn404_whenCardNotFound() throws Exception {
        when(cardService.getCardHistory(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Tarjeta", 99L));

        mockMvc.perform(get("/api/cards/99/history"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_shouldReturn200_whenAdmin() throws Exception {
        cardResponse.setStatus(CardStatus.BLOCKED);
        when(cardService.blockCard(1L)).thenReturn(cardResponse);

        mockMvc.perform(patch("/api/cards/1/block").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCard_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/cards/1/block").with(csrf()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).blockCard(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_shouldReturn422_whenAlreadyBlocked() throws Exception {
        when(cardService.blockCard(1L))
                .thenThrow(new BusinessException("La tarjeta ya está bloqueada."));

        mockMvc.perform(patch("/api/cards/1/block").with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("La tarjeta ya está bloqueada."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unblockCard_shouldReturn200_whenAdmin() throws Exception {
        when(cardService.unblockCard(1L)).thenReturn(cardResponse);

        mockMvc.perform(patch("/api/cards/1/unblock").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void unblockCard_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/cards/1/unblock").with(csrf()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).unblockCard(anyLong());
    }
}