package com.urbanpass.urbanpass.controller;

import com.urbanpass.urbanpass.dto.CardResponse;
import com.urbanpass.urbanpass.dto.CreateUserRequest;
import com.urbanpass.urbanpass.dto.UserResponse;
import com.urbanpass.urbanpass.service.CardService;
import com.urbanpass.urbanpass.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    // Inyectá CardService arriba junto a UserService
    private final CardService cardService;

    // POST /api/users/{id}/cards → Emitir tarjeta
    @PostMapping("/{id}/cards")
    public ResponseEntity<CardResponse> issueCard(@PathVariable Long id) {
        CardResponse card = cardService.issueCard(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    // GET /api/users/{id}/cards → Ver tarjetas del usuario
    @GetMapping("/{id}/cards")
    public ResponseEntity<List<CardResponse>> getUserCards(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardsByUser(id));
    }
}