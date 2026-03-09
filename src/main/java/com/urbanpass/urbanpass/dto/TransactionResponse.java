package com.urbanpass.urbanpass.dto;

import com.urbanpass.urbanpass.enums.TransactionStatus;
import com.urbanpass.urbanpass.enums.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long cardId;
    private String cardNumber;
    private String stationName;   // null si es recarga
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime timestamp;
}