// Purpose: Response DTO for credit limit check
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/CreditLimitCheckResponse.java

package com.nextgenloan.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLimitCheckResponse {

    private boolean approved;
    private String message;
    private BigDecimal availableLimit;
    private BigDecimal requestedAmount;
    private String reservationId;
    private String loanNumber;
}
