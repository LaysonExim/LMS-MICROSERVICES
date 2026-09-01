// Purpose: DTO for loan repayments
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/LoanRepaymentDto.java

package com.nextgenloan.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentDto {
    private String repaymentReference;
    private BigDecimal amount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private LocalDateTime repaymentDate;
    private String paymentMethod;
    private String status;
    private String notes;
}