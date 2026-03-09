package com.urbanpass.urbanpass.dto;

import com.urbanpass.urbanpass.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ValidationResponse {
    private boolean approved;        // ¿Pasó el torniquete?
    private String message;          // Mensaje para mostrar
    private BigDecimal balanceAfter; // Saldo restante
    private Long transactionId;      // ID para auditoría
    private TransactionStatus status;
    private LocalDateTime timestamp;
}