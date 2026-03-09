package com.urbanpass.urbanpass.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateCardRequest {

    @NotNull(message = "La estación es obligatoria")
    private Long stationId;
}