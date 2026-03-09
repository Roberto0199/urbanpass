package com.urbanpass.urbanpass.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RechargeRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "10.00", message = "Recarga mínima: Q10.00")
    @DecimalMax(value = "500.00", message = "Recarga máxima: Q500.00")
    private BigDecimal amount;
}