package com.urbanpass.urbanpass;

import com.urbanpass.urbanpass.dto.CardResponse;
import com.urbanpass.urbanpass.dto.RechargeRequest;
import com.urbanpass.urbanpass.dto.ValidationResponse;
import com.urbanpass.urbanpass.dto.ValidateCardRequest;
import com.urbanpass.urbanpass.entity.Card;
import com.urbanpass.urbanpass.entity.Station;
import com.urbanpass.urbanpass.entity.User;
import com.urbanpass.urbanpass.enums.CardStatus;
import com.urbanpass.urbanpass.repository.CardRepository;
import com.urbanpass.urbanpass.repository.StationRepository;
import com.urbanpass.urbanpass.repository.TransactionRepository;
import com.urbanpass.urbanpass.repository.UserRepository;
import com.urbanpass.urbanpass.service.CardService;
import com.urbanpass.urbanpass.service.TransactionHelper;
import com.urbanpass.urbanpass.exception.BusinessException;
import com.urbanpass.urbanpass.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    // 🎭 Mocks — simulan las dependencias sin tocar la DB
    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private StationRepository stationRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionHelper transactionHelper;

    // 🎯 El servicio real que vamos a testear
    @InjectMocks
    private CardService cardService;

    // Objetos de prueba reutilizables
    private User testUser;
    private Card testCard;
    private Station testStation;

    @BeforeEach
    void setUp() {
        // Este método se ejecuta antes de CADA test
        // Preparamos datos de prueba limpios

        testUser = User.builder()
                .id(1L)
                .name("Carlos Pérez")
                .email("carlos@gmail.com")
                .phone("55551234")
                .build();

        testCard = Card.builder()
                .id(1L)
                .user(testUser)
                .cardNumber("5200123456789012")
                .balance(new BigDecimal("50.00"))
                .status(CardStatus.ACTIVE)
                .version(0L)
                .build();

        testStation = Station.builder()
                .id(1L)
                .name("Centra Norte")
                .location("Zona 18")
                .isActive(true)
                .build();
    }

    // ================================================================
    //  TESTS DE RECARGA
    // ================================================================

    @Test
    void recharge_shouldIncreaseBalance_whenCardIsActive() {
        // ARRANGE — preparar
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("30.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(transactionRepository.save(any())).thenReturn(null);

        // ACT — ejecutar
        CardResponse response = cardService.recharge(1L, request);

        // ASSERT — verificar
        assertNotNull(response);
        assertEquals(new BigDecimal("80.00"), testCard.getBalance());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void recharge_shouldThrowException_whenCardIsBlocked() {
        // ARRANGE
        testCard.setStatus(CardStatus.BLOCKED);
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("30.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // ACT & ASSERT
        assertThrows(BusinessException.class, () -> {
            cardService.recharge(1L, request);
        });

        // Verificar que NUNCA se guardó nada
        verify(cardRepository, never()).save(any());
    }

    @Test
    void recharge_shouldThrowException_whenCardNotFound() {
        // ARRANGE
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("30.00"));

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            cardService.recharge(99L, request);
        });
    }

    // ================================================================
    //  TESTS DE VALIDACIÓN (TORNIQUETE)
    // ================================================================

    @Test
    void validateCard_shouldDeductFare_whenCardIsActiveAndHasBalance() {
        // ARRANGE
        ValidateCardRequest request = new ValidateCardRequest();
        request.setStationId(1L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(stationRepository.findById(1L)).thenReturn(Optional.of(testStation));
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any())).thenReturn(testCard);
        when(transactionRepository.save(any())).thenReturn(null);

        BigDecimal balanceAntes = testCard.getBalance();

        // ACT
        ValidationResponse response = cardService.validateCard(1L, request);

        // ASSERT
        assertTrue(response.isApproved());
        assertEquals("¡Bienvenido! Buen viaje.", response.getMessage());
        assertTrue(testCard.getBalance().compareTo(balanceAntes) < 0);
    }

    @Test
    void validateCard_shouldFail_whenCardIsBlocked() {
        // ARRANGE
        testCard.setStatus(CardStatus.BLOCKED);

        ValidateCardRequest request = new ValidateCardRequest();
        request.setStationId(1L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(stationRepository.findById(1L)).thenReturn(Optional.of(testStation));

        // ACT & ASSERT
        assertThrows(BusinessException.class, () -> {
            cardService.validateCard(1L, request);
        });

        // Verificar que el saldo NO se modificó
        assertEquals(new BigDecimal("50.00"), testCard.getBalance());
    }

    @Test
    void validateCard_shouldFail_whenInsufficientBalance() {
        // ARRANGE
        testCard.setBalance(new BigDecimal("1.00")); // menos que la tarifa

        ValidateCardRequest request = new ValidateCardRequest();
        request.setStationId(1L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(stationRepository.findById(1L)).thenReturn(Optional.of(testStation));

        // ACT & ASSERT
        assertThrows(BusinessException.class, () -> {
            cardService.validateCard(1L, request);
        });
    }

    // ================================================================
    //  TESTS DE BLOQUEO
    // ================================================================

    @Test
    void blockCard_shouldChangeStatus_whenCardIsActive() {
        // ARRANGE
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any())).thenReturn(testCard);

        // ACT
        CardResponse response = cardService.blockCard(1L);

        // ASSERT
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository, times(1)).save(any());
    }

    @Test
    void blockCard_shouldThrowException_whenCardAlreadyBlocked() {
        // ARRANGE
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // ACT & ASSERT
        assertThrows(BusinessException.class, () -> {
            cardService.blockCard(1L);
        });
    }
}
