package com.urbanpass.urbanpass.service;

import com.urbanpass.urbanpass.dto.*;
import com.urbanpass.urbanpass.entity.Card;
import com.urbanpass.urbanpass.entity.Station;
import com.urbanpass.urbanpass.entity.Transaction;
import com.urbanpass.urbanpass.entity.User;
import com.urbanpass.urbanpass.enums.CardStatus;
import com.urbanpass.urbanpass.enums.TransactionStatus;
import com.urbanpass.urbanpass.enums.TransactionType;
import com.urbanpass.urbanpass.repository.CardRepository;
import com.urbanpass.urbanpass.repository.StationRepository;
import com.urbanpass.urbanpass.repository.TransactionRepository;
import com.urbanpass.urbanpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final TransactionRepository transactionRepository;

    private final TransactionHelper transactionHelper; // ← agregá esta línea

    // ── Emitir tarjeta a un usuario ──────────────────────────
    @Transactional
    public CardResponse issueCard(Long userId) {
        log.info("Emitiendo tarjeta para usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Card card = Card.builder()
                .user(user)
                .cardNumber(generateCardNumber())
                .status(CardStatus.ACTIVE)
                .build();

        Card saved = cardRepository.save(card);
        log.info("Tarjeta emitida: {}", saved.getCardNumber());

        return toResponse(saved);
    }

    // ── Ver todas las tarjetas de un usuario ─────────────────
    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado: " + userId);
        }
        return cardRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers privados ─────────────────────────────────────
    private String generateCardNumber() {
        String number;
        Random random = new Random();
        do {
            number = "5200" + String.format("%012d", Math.abs(random.nextLong()) % 1_000_000_000_000L);
            number = number.substring(0, 16);
        } while (cardRepository.existsByCardNumber(number));
        return number;
    }

    public CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .balance(card.getBalance())
                .status(card.getStatus())
                .userId(card.getUser().getId())
                .userName(card.getUser().getName())
                .createdAt(card.getCreatedAt())
                .build();
    }
    @Transactional
    public CardResponse recharge(Long cardId, RechargeRequest request) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada: " + cardId));

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new IllegalArgumentException("La tarjeta no está activa.");
        }

        // PASO 1 — Actualizar saldo
        card.setBalance(card.getBalance().add(request.getAmount()));
        cardRepository.save(card);

        // PASO 2 — Guardar transacción con el saldo ya actualizado
        Transaction tx = Transaction.builder()
                .card(card)
                .amount(request.getAmount())
                .type(TransactionType.RECHARGE)
                .status(TransactionStatus.SUCCESS)
                .balanceAfter(card.getBalance()) // ← ahora sí tiene el saldo correcto
                .description("Recarga de saldo")
                .build();

        transactionRepository.save(tx);

        return toResponse(card);
    }


    // Tarifa fija por viaje
    private static final BigDecimal TARIFA = new BigDecimal("5.00");

    // ── Validación en torniquete ─────────────────────────────
    @Transactional
    public ValidationResponse validateCard(Long cardId, ValidateCardRequest request) {
        log.info("Validando tarjeta ID: {} en estación ID: {}", cardId, request.getStationId());

        // PASO 1 — Buscar sin lock para verificaciones rápidas
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada: " + cardId));

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Estación no encontrada: " + request.getStationId()));

        // PASO 2 — ¿La tarjeta está activa?
        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            transactionHelper.saveFailedTransaction(card, station, TARIFA, "Tarjeta bloqueada");
            throw new IllegalArgumentException("La tarjeta está bloqueada o inactiva.");
        }

        // PASO 3 — ¿Tiene saldo suficiente?
        if (card.getBalance().compareTo(TARIFA) < 0) {
            transactionHelper.saveFailedTransaction(card, station, TARIFA, "Saldo insuficiente");
            throw new IllegalArgumentException(
                    "Saldo insuficiente. Saldo actual: Q" + card.getBalance() + " | Requerido: Q" + TARIFA
            );
        }

        // PASO 4 — Ahora sí usamos lock pesimista para descontar
        // Solo llegamos aquí si la tarjeta está activa y tiene saldo
        Card lockedCard = cardRepository.findByIdWithLock(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada: " + cardId));

        lockedCard.setBalance(lockedCard.getBalance().subtract(TARIFA));
        cardRepository.save(lockedCard);

        // PASO 5 — Registrar transacción exitosa
        Transaction tx = Transaction.builder()
                .card(lockedCard)
                .station(station)
                .amount(TARIFA)
                .type(TransactionType.VALIDATION)
                .status(TransactionStatus.SUCCESS)
                .balanceAfter(lockedCard.getBalance())
                .description("Validación en " + station.getName())
                .build();
        transactionRepository.save(tx);

        log.info("Validación EXITOSA. Tarjeta: {} | Saldo restante: Q{}",
                lockedCard.getCardNumber(), lockedCard.getBalance());

        return ValidationResponse.builder()
                .approved(true)
                .message("¡Bienvenido! Buen viaje.")
                .balanceAfter(lockedCard.getBalance())
                .transactionId(tx.getId())
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();
    }
    // ── Bloquear tarjeta ─────────────────────────────────────
    @Transactional
    public CardResponse blockCard(Long cardId) {
        log.info("Bloqueando tarjeta ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada: " + cardId));

        if (card.getStatus().equals(CardStatus.BLOCKED)) {
            throw new IllegalArgumentException("La tarjeta ya está bloqueada.");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        log.info("Tarjeta {} bloqueada exitosamente.", card.getCardNumber());
        return toResponse(card);
    }

    // ── Desbloquear tarjeta ──────────────────────────────────
    @Transactional
    public CardResponse unblockCard(Long cardId) {
        log.info("Desbloqueando tarjeta ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada: " + cardId));

        if (card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new IllegalArgumentException("La tarjeta ya está activa.");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        log.info("Tarjeta {} desbloqueada exitosamente.", card.getCardNumber());
        return toResponse(card);
    }
    // ── Historial de transacciones ───────────────────────────
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getCardHistory(Long cardId, Pageable pageable) {

        if (!cardRepository.existsById(cardId)) {
            throw new RuntimeException("Tarjeta no encontrada: " + cardId);
        }

        return transactionRepository
                .findByCardIdOrderByTimestampDesc(cardId, pageable)
                .map(this::toTransactionResponse);
    }

    // Mapper privado: Transaction → DTO
    private TransactionResponse toTransactionResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .cardId(tx.getCard().getId())
                .cardNumber(tx.getCard().getCardNumber())
                .stationName(tx.getStation() != null ? tx.getStation().getName() : null)
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus())
                .balanceAfter(tx.getBalanceAfter())
                .description(tx.getDescription())
                .timestamp(tx.getTimestamp())
                .build();
    }
}