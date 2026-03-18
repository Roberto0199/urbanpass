package com.urbanpass.urbanpass.controller;

import com.urbanpass.urbanpass.dto.CardResponse;
import com.urbanpass.urbanpass.dto.CreateUserRequest;
import com.urbanpass.urbanpass.dto.UserResponse;
import com.urbanpass.urbanpass.service.CardService;
import com.urbanpass.urbanpass.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestión de usuarios")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final CardService cardService;

    @Operation(summary = "Crear usuario", description = "Solo administradores.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Listar todos los usuarios", description = "Solo administradores.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Emitir tarjeta a usuario", description = "Solo administradores.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cards")
    public ResponseEntity<CardResponse> issueCard(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.issueCard(id));
    }

    @Operation(summary = "Ver tarjetas de un usuario")
    @GetMapping("/{id}/cards")
    public ResponseEntity<List<CardResponse>> getUserCards(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardsByUser(id));
    }
}