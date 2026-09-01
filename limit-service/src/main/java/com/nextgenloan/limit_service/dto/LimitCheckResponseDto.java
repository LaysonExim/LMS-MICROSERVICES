// Purpose: Response for limit check
// File: limit-service/src/main/java/com/nextgenloan/limit/dto/LimitCheckResponseDto.java

package com.nextgenloan.limit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitCheckResponseDto {
    private boolean approved;
    private String message;
    private BigDecimal availableLimit;
    private BigDecimal requestedAmount;
    private String reservationId;
}