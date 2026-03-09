package com.urbanpass.urbanpass.dto;

import com.urbanpass.urbanpass.enums.CardStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CardResponse {
    private Long id;
    private String cardNumber;
    private BigDecimal balance;
    private CardStatus status;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}