package com.urbanpass.urbanpass.controller;

import com.urbanpass.urbanpass.dto.*;
import com.urbanpass.urbanpass.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // POST /api/cards/{cardId}/recharge
    @PostMapping("/{cardId}/recharge")
    public ResponseEntity<CardResponse> recharge(
            @PathVariable Long cardId,
            @Valid @RequestBody RechargeRequest request) {

        CardResponse response = cardService.recharge(cardId, request);
        return ResponseEntity.ok(response);
    }

    // POST /api/cards/{cardId}/validate
    @PostMapping("/{cardId}/validate")
    public ResponseEntity<ValidationResponse> validateCard(
            @PathVariable Long cardId,
            @Valid @RequestBody ValidateCardRequest request) {

        ValidationResponse response = cardService.validateCard(cardId, request);
        return ResponseEntity.ok(response);

    }
    // GET /api/cards/{cardId}/history
    @GetMapping("/{cardId}/history")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getCardHistory(cardId, pageable));
    }
    // PATCH /api/cards/{cardId}/block
    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    // PATCH /api/cards/{cardId}/unblock
    @PatchMapping("/{cardId}/unblock")
    public ResponseEntity<CardResponse> unblockCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.unblockCard(cardId));
    }
}