// Purpose: Request DTO for credit limit check
// File: loan-service/src/main/java/com/nextgenloan/loan/dto/CreditLimitCheckRequest.java

package com.nextgenloan.loan_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLimitCheckRequest {

    @NotBlank(message = "Customer number is required")
    private String customerNumber;

    @NotBlank(message = "Product type is required")
    private String productType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String loanNumber;
}
