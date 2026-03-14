package com.urbanpass.urbanpass.controller;

import com.urbanpass.urbanpass.dto.*;
import com.urbanpass.urbanpass.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cards", description = "Gestión de tarjetas: recargas, validaciones y bloqueos")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {

    private final CardService cardService;

    // POST /api/cards/{cardId}/recharge
    @Operation(
            summary = "Recargar saldo",
            description = "Agrega saldo a una tarjeta activa. Mínimo Q10.00, máximo Q500.00."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recarga exitosa",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Tarjeta bloqueada o inactiva",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Monto inválido",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class)))
    })
    @PostMapping("/{cardId}/recharge")
    public ResponseEntity<CardResponse> recharge(
            @Parameter(description = "ID de la tarjeta", example = "1")
            @PathVariable Long cardId,
            @Valid @RequestBody RechargeRequest request) {

        return ResponseEntity.ok(cardService.recharge(cardId, request));
    }

    // POST /api/cards/{cardId}/validate
    @Operation(
            summary = "Validar en torniquete",
            description = "Descuenta la tarifa (Q5.00) si la tarjeta está activa y tiene saldo suficiente. " +
                    "Usa bloqueo pesimista para evitar cobros dobles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validación aprobada — pase al bus",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarjeta o estación no encontrada",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Tarjeta bloqueada o saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class)))
    })
    @PostMapping("/{cardId}/validate")
    public ResponseEntity<ValidationResponse> validateCard(
            @Parameter(description = "ID de la tarjeta", example = "1")
            @PathVariable Long cardId,
            @Valid @RequestBody ValidateCardRequest request) {

        return ResponseEntity.ok(cardService.validateCard(cardId, request));
    }

    // GET /api/cards/{cardId}/history
    @Operation(
            summary = "Historial de transacciones",
            description = "Devuelve todas las transacciones de una tarjeta, ordenadas por fecha descendente. Soporta paginación."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class)))
    })
    @GetMapping("/{cardId}/history")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @Parameter(description = "ID de la tarjeta", example = "1")
            @PathVariable Long cardId,
            @Parameter(description = "Número de página (inicia en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Resultados por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getCardHistory(cardId, pageable));
    }

    // PATCH /api/cards/{cardId}/block
    @Operation(
            summary = "Bloquear tarjeta",
            description = "Bloquea una tarjeta activa. Una tarjeta bloqueada no puede validarse ni recargarse."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta bloqueada exitosamente",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "La tarjeta ya está bloqueada",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class)))
    })
    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardResponse> blockCard(
            @Parameter(description = "ID de la tarjeta", example = "1")
            @PathVariable Long cardId) {

        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    // PATCH /api/cards/{cardId}/unblock
    @Operation(
            summary = "Desbloquear tarjeta",
            description = "Reactiva una tarjeta bloqueada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta desbloqueada exitosamente",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "La tarjeta ya está activa",
                    content = @Content(schema = @Schema(implementation = com.urbanpass.urbanpass.exception.ErrorResponse.class)))
    })
    @PatchMapping("/{cardId}/unblock")
    public ResponseEntity<CardResponse> unblockCard(
            @Parameter(description = "ID de la tarjeta", example = "1")
            @PathVariable Long cardId) {

        return ResponseEntity.ok(cardService.unblockCard(cardId));
    }
}