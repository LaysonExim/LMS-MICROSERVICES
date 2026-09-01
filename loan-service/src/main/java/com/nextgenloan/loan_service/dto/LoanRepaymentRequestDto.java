// Purpose: DTO for repayment requests
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/LoanRepaymentRequestDto.java

package com.nextgenloan.loan_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentRequestDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 30, message = "Payment method must be at most 30 characters")
    private String paymentMethod;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;
}